package cn.piesat.nj.slardar.starter;

import cn.piesat.nj.misc.hutool.mini.StringUtil;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.spi.SlardarSpiContext;
import cn.piesat.nj.slardar.spi.SlardarSpiFactory;
import cn.piesat.nj.slardar.spi.authenticate.SlardarAuthenticateResultAdapter;
import cn.piesat.nj.slardar.spi.token.SlardarTokenProvider;
import cn.piesat.nj.slardar.starter.config.SlardarProperties;
import cn.piesat.nj.slardar.starter.handler.SlardarDefaultAuthenticateResultAdapter;
import cn.piesat.nj.slardar.starter.support.HttpServletUtil;
import cn.piesat.nj.slardar.starter.support.LoginConcurrentPolicy;
import cn.piesat.nj.slardar.starter.support.LoginDeviceType;
import com.google.common.base.Joiner;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.api.sync.RedisSetCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static cn.piesat.nj.slardar.core.Constants.BEARER;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;


/**
 * <p>
 * 认证service
 * - token
 * - jwt token 是token 实现之一
 * - 此模块内实现 token 生成 注销 刷新等
 * - 同端互斥 多端并存 实现
 * - 认证结果处理
 * `- succeed
 * - fail
 * - deny
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/26
 */
public class SlardarAuthenticateService {

    private final SlardarProperties slardarProperties;

    private final SlardarSpiFactory spiFactory;

    private static final String TOKEN_KEY_PREFIX = "account_token";

    private static final Joiner UNDERLINE_JOINER = Joiner.on("_");

    private final RedisClient redisClient;

    private final RedisCommands<String, String> stringCommands;

    private final RedisSetCommands<String, String> setCommands;

    private final SlardarSpiContext slardarContext;

    public static final Logger log = LoggerFactory.getLogger(SlardarAuthenticateService.class);


    public SlardarAuthenticateService(SlardarProperties slardarProperties, SlardarSpiFactory spiFactory,
                                      SlardarSpiContext context, RedisClient redisClient) {
        this.slardarProperties = slardarProperties;
        this.spiFactory = spiFactory;
        this.slardarContext = context;
        this.redisClient = redisClient;
        this.stringCommands = redisClient.connect().sync();
        this.setCommands = redisClient.connect().sync();
    }


    /**
     * get token value from request
     *
     * @param request
     * @return 不含前缀
     */
    public String getTokenValue(HttpServletRequest request) {
        String tokenValue = null;
        String tokenKey = slardarProperties.getToken().getKey();
        // 1. 尝试从request attributes里读取
        Object attribute = request.getAttribute(tokenKey);
        if (attribute != null) {
            tokenValue = String.valueOf(attribute);
        }
        if (Objects.isNull(tokenValue)) {
            // 2. 尝试从请求体里面读取
            tokenValue = request.getParameter(tokenKey);
        }
        if (Objects.isNull(tokenValue)) {
            // 3. 尝试从header里读取
            tokenValue = request.getHeader(tokenKey);
        }
        if (Objects.isNull(tokenValue)) {
            // 4. 尝试从cookie里读取
            tokenValue = HttpServletUtil.getCookieValue(request, tokenKey);
        }
        if (tokenValue != null && tokenValue.startsWith(BEARER)) {
            tokenValue = tokenValue.replace(BEARER, "");
        }
        return tokenValue;
    }

    /**
     * 注入 token value 到 request/response header/cookie/ ...
     *
     * @param tokenValue
     */
    public void setTokenValue(String tokenValue, @NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        if (StringUtils.isEmpty(tokenValue)) {
            return;
        }
        String tokenKey = slardarProperties.getToken().getKey();
        // 1. 将 Token 保存到 [存储器] 里
        request.setAttribute(tokenKey, tokenValue);
        // 2. 将 Token 保存到 [Cookie] 里
        HttpServletUtil.setCookie(response, tokenKey, tokenValue, 3600 * 24, "", "", "Strict");
        // 3. 将 Token 写入到响应头里
        response.setHeader(tokenKey, tokenValue);
        response.addHeader(ACCESS_CONTROL_EXPOSE_HEADERS, tokenKey);
    }

    /**
     * token 是否已过期
     *
     * @param tokenValue
     * @param deviceType
     * @return
     */
    public boolean isExpired(String tokenValue, LoginDeviceType deviceType) {
        String username = getTokenImpl().getSubject(tokenValue);
        if (Objects.isNull(username)) {
            return true;
        }
        return !hasFromRedis(key(username, deviceType));
    }

    /**
     * token 剩余有效时间
     * @param tokenValue
     * @param deviceType
     * @return
     */
    public long ttl(String tokenValue, LoginDeviceType deviceType) {
        String username = getTokenImpl().getSubject(tokenValue);
        if (Objects.isNull(username)) {
            return 0L;
        }
        return stringCommands.ttl(key(username, deviceType));
    }



    public boolean isExpired(String tokenValue) {
        return isExpired(tokenValue, LoginDeviceType.PC);
    }

    /**
     * FIXME:
     * get username from token
     *
     * @param tokenValue
     * @return
     */
    public String getUsername(String tokenValue) {
        String usernameWithId = getTokenImpl().getSubject(tokenValue);
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
    public SlardarTokenProvider.Payload createToken(@NonNull String username, @NonNull LoginDeviceType deviceType, @NonNull LoginConcurrentPolicy concurrentPolicy) {
        switch (concurrentPolicy) {
            case mutex:
                // 移除同端的token 重新生成
                removeTokens(username, deviceType);
                break;
            case share:
                SlardarTokenProvider.Payload existedToken = getExistedToken(username, deviceType);
                if (StringUtils.hasText(existedToken.getTokenValue())) {
                    return existedToken;
                }
                break;
            default:
            case separate:
                break;
        }
        String id = simpleUUID();
        // xxx_id
        String usernameKey = UNDERLINE_JOINER.join(username, id);
        SlardarTokenProvider.Payload payload = getTokenImpl().generate(usernameKey);
        // TODO: into store
        // 有效期也需要返回给客户端 用户缓存等
        setCommands.sadd(username, id);
        stringCommands.setex(key(usernameKey, deviceType), Duration.between(LocalDateTime.now(), payload.getExpiresAt()).getSeconds(),
                payload.getTokenValue());
        return payload;
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
    public SlardarTokenProvider.Payload refreshToken(String tokenValue, LoginDeviceType deviceType) {
        String username = getTokenImpl().getSubject(tokenValue);
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
        String username = getTokenImpl().getSubject(tokenValue);
        String key = key(username, deviceType);
        String existedToken = getFromRedis(key);
        if (StringUtils.isEmpty(existedToken)) {
            log.error("[authz] 续期失败, key 为 [{}] 的token 不存在", key);
            return false;
        }
        // TODO: 需要转移到具体实现类里
        stringCommands.setex(key, getTokenImpl().getExpiration(), existedToken);
        return true;
    }

    /**
     * 简化的UUID，去掉了横线
     *
     * @return 简化的UUID，去掉了横线
     */
    private String simpleUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 从存储中获取已存在的 token 值
     *
     * @param username
     * @param deviceType
     * @return 返回 空 表示不存在 或 已过期
     */
    private SlardarTokenProvider.Payload getExistedToken(String username, LoginDeviceType deviceType) {
        // 取第一个以 username_xx 为key的值
        Set<String> ids = setCommands.smembers(username);
        String redisKey = "";
        if (ids != null && ids.size() > 0) {
            redisKey = key(UNDERLINE_JOINER.join(username, ids.iterator().next()), deviceType);
        } else {
            redisKey = key(username, deviceType);
        }
        // 返回剩余秒数
        Long remainSeconds = stringCommands.ttl(redisKey);
        String tokenValue = getFromRedis(redisKey);
        return new SlardarTokenProvider.Payload().setTokenValue(tokenValue).setExpiresAt(LocalDateTime.now().plusSeconds(remainSeconds));
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
        return UNDERLINE_JOINER.join(TOKEN_KEY_PREFIX, deviceType.name(), username);
    }


    private String getFromRedis(String key) {
        return stringCommands.get(key);
    }

    private boolean hasFromRedis(String key) {
        return stringCommands.exists(key) > 0;
    }


    private SlardarTokenProvider getTokenImpl() {
        try {
            return spiFactory.findTokenProvider(this.slardarProperties.getToken().getType());
        } catch (SlardarException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取默认的结果处理
     *
     * @return
     */
    public SlardarAuthenticateResultAdapter getAuthResultHandler() {
        String resultHandlerType = slardarProperties.getLogin().getResultHandlerType();
        return getAuthResultHandler(StringUtil.isBlank(resultHandlerType) ? SlardarDefaultAuthenticateResultAdapter.NAME : resultHandlerType);
    }

    /**
     * 获取自定义的结果处理
     *
     * @param name
     * @return
     */
    public SlardarAuthenticateResultAdapter getAuthResultHandler(String name) {
        try {
            return spiFactory.findAuthenticateResultHandler(name);
        } catch (SlardarException e) {
            throw new RuntimeException(e);
        }
    }
}
