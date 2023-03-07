package cn.piesat.nj.slardar.starter.token;

import cn.hutool.core.util.RandomUtil;
import cn.piesat.nj.skv.core.KvStore;
import cn.piesat.v.authx.security.infrastructure.spring.SecurityProperties;
import cn.piesat.v.authx.security.infrastructure.spring.support.LoginConcurrentPolicy;
import cn.piesat.v.authx.security.infrastructure.spring.support.LoginDeviceType;
import com.google.common.base.Joiner;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.api.sync.RedisSetCommands;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;


/**
 * <p>
 * 登录后认证 token 处理
 * - jwt token 是token 实现之一
 * - 此模块内实现 token 生成 注销 刷新等
 * - 同端互斥 多段并存 实现
 * - ....
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/26
 */
@Slf4j
@Component
public class AuthxTokenService {


    private final SecurityProperties securityProperties;

    private final AuthxToken authToken;

    public static final String token_key_prefix = "account_token";

    public static final Joiner UNDERLINE_JOINER = Joiner.on("_");

    private final KvStore kvStore;

    private final RedisClient redisClient;

    private final RedisCommands<String, String> stringCommands;

    private final RedisSetCommands<String, String> setCommands;


    public AuthxTokenService(SecurityProperties securityProperties, KvStore kvStore, RedisClient redisClient) {
        this.securityProperties = securityProperties;
        // TBD: 应当动态注入实现
        this.authToken = new JwtAuthxToken(securityProperties);
        this.kvStore = kvStore;
        this.redisClient = redisClient;
        this.stringCommands = redisClient.connect().sync();
        this.setCommands = redisClient.connect().sync();
    }

    /**
     * token 是否已过期
     *
     * @param tokenValue
     * @param deviceType
     * @return
     */
    public boolean isExpired(String tokenValue, LoginDeviceType deviceType) {
        String username = authToken.getSubjectFromToken(tokenValue);
        return !hasFromRedis(key(username, deviceType));
    }

    /**
     * get username from token
     *
     * @param tokenValue
     * @return
     */
    public String getUsername(String tokenValue) {
        String usernameWithId = authToken.getSubjectFromToken(tokenValue);
        return usernameWithId.split("_")[0];
    }

    /**
     * 考虑同端策略
     *
     * @param username
     * @param deviceType
     * @param concurrentPolicy
     * @return
     */
    public String createToken(@NonNull String username, @NonNull LoginDeviceType deviceType, @NonNull LoginConcurrentPolicy concurrentPolicy) {
        switch (concurrentPolicy) {
            case mutex:
                // 移除同端的token 重新生成
                removeTokens(username, deviceType);
                break;
            case share:
                String existedToken = getExistedToken(username, deviceType);
                if (StringUtils.hasText(existedToken)) {
                    return existedToken;
                }
                break;
            default:
            case separate:
                break;
        }
        String id = RandomUtil.simpleUUID();
        // xxx_id
        String usernameKey = UNDERLINE_JOINER.join(username, id);
        AuthxToken.Payload payload = authToken.generateToken(usernameKey);
        // TODO: into store
        setCommands.sadd(username, id);
        stringCommands.setex(key(usernameKey, deviceType), Duration.between(LocalDateTime.now(), payload.getExpiresAt()).getSeconds(),
                payload.getTokenValue());
        return payload.getTokenValue();
    }

    /**
     * 注销同设备 同账号 所有 tokens
     *
     * @param username
     * @param deviceType
     * @return
     */
    public boolean removeTokens(@NonNull String username, @NonNull LoginDeviceType deviceType) {
        Set<String> ids = setCommands.smembers(username);
        if (ids == null) {
            return true;
        }
        if (ids.size() == 0) {
            return true;
        }
        return ids.stream().allMatch(id -> stringCommands.del(key(username + "_" + id, deviceType)) != null &&
                setCommands.srem(username, id) != null);
    }

    /**
     * 刷新 token 替换原先的 token
     * 客户端需要更新 token 值
     *
     * @param tokenValue
     * @return
     */
    public String refreshToken(String tokenValue, LoginDeviceType deviceType) {
        String username = authToken.getSubjectFromToken(tokenValue);
        String key = key(username, deviceType);
        String existedToken = getFromRedis(key);
        // 生成新的
        if (StringUtils.hasText(existedToken)) {
            stringCommands.del(key);
        }
        return createToken(username, deviceType, null);
    }

    /**
     * 同一个 token 续期, 延长过期 但不会更新 token值
     *
     * @param tokenValue
     * @param deviceType
     * @return
     */
    public boolean renewToken(String tokenValue, LoginDeviceType deviceType) {
        String username = authToken.getSubjectFromToken(tokenValue);
        String key = key(username, deviceType);
        String existedToken = getFromRedis(key);
        if (StringUtils.isEmpty(existedToken)) {
            log.error("[authz] 续期失败, key 为 [{}] 的token 不存在", key);
            return false;
        }
        stringCommands.setex(key, securityProperties.getJwt().getExpiration(), existedToken);
        return true;
    }

    /**
     * 从存储中获取已存在的 token 值
     *
     * @param username
     * @param deviceType
     * @return 返回 空 表示不存在 或 已过期
     */
    private String getExistedToken(String username, LoginDeviceType deviceType) {
        // 取第一个以 username_xx 为key的值
        Set<String> ids = setCommands.smembers(username);
        if (ids != null && ids.size() > 0) {
            return getFromRedis(key(UNDERLINE_JOINER.join(username, ids.iterator().next()), deviceType));
        }
        return getFromRedis(key(username, deviceType));
    }

    /**
     * 如果是同端多次登录且可以并存
     * key like ： xxx_PC_user01 xxx_PC_user02 ...
     *
     * @param username
     * @param deviceType
     * @return
     */
    private String key(String username, LoginDeviceType deviceType) {
        return UNDERLINE_JOINER.join(token_key_prefix, deviceType.name(), username);
    }


    private String getFromRedis(String key) {
        return stringCommands.get(key);
    }

    private boolean hasFromRedis(String key) {
        return stringCommands.exists(key) > 0;
    }


}
