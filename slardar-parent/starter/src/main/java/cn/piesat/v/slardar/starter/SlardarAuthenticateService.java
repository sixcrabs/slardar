package cn.piesat.v.slardar.starter;

import cn.piesat.v.slardar.spi.SlardarKeyStore;
import cn.piesat.v.slardar.spi.SlardarSpiFactory;
import cn.piesat.v.slardar.spi.authenticate.SlardarAuthenticateResultAdapter;
import cn.piesat.v.slardar.spi.token.SlardarTokenProvider;
import cn.piesat.v.slardar.starter.config.SlardarProperties;
import cn.piesat.v.slardar.starter.handler.SlardarDefaultAuthenticateResultAdapter;
import cn.piesat.v.slardar.starter.support.HttpServletUtil;
import cn.piesat.v.slardar.starter.support.LoginConcurrentPolicy;
import cn.piesat.v.slardar.starter.support.LoginDeviceType;
import cn.piesat.v.misc.hutool.mini.StringUtil;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
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

import static cn.piesat.v.slardar.core.Constants.BEARER;
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
 * TODO: 使用 keystore
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/26
 */
public class SlardarAuthenticateService {

    private final SlardarProperties slardarProperties;

    private final SlardarSpiFactory spiFactory;

    private static final String TOKEN_KEY_PREFIX = "slardar_token";

    private final Joiner keyJoiner;

    private final Splitter keySplitter;

    private RedisCommands<String, String> stringCommands;

    private RedisSetCommands<String, String> setCommands;

    public static final Logger log = LoggerFactory.getLogger(SlardarAuthenticateService.class);

    private final SlardarKeyStore keyStore;

    public SlardarAuthenticateService(SlardarProperties slardarProperties, SlardarSpiFactory spiFactory) {
        this.slardarProperties = slardarProperties;
        this.spiFactory = spiFactory;
        this.keyStore = spiFactory.findKeyStore(slardarProperties.getKeyStore().getType());
//        this.redisClient = redisClient;
//        try {
//            this.stringCommands = redisClient.connect().sync();
//            this.setCommands = redisClient.connect().sync();
//        } catch (Exception e) {
//            log.error("redis error: {}", e.getLocalizedMessage());
//        }
        this.keyJoiner = Joiner.on(slardarProperties.getToken().getSeparator());
        this.keySplitter = Splitter.on(slardarProperties.getToken().getSeparator());
    }


    /**
     * token 是否已过期
     *
     * @param tokenValue
     * @param deviceType
     * @return
     */
    public boolean isExpired(String tokenValue, LoginDeviceType deviceType) {
        String userKey = getUserKeyFromTokenValue(tokenValue);
        if (Objects.isNull(userKey)) {
            return true;
        }
        return !keyStore.has(generateTokenKey(userKey, deviceType));
    }

    /**
     * token 剩余有效时间
     *
     * @param tokenValue
     * @param deviceType
     * @return
     */
    public long ttl(String tokenValue, LoginDeviceType deviceType) {
        String username = getUserKeyFromTokenValue(tokenValue);
        if (Objects.isNull(username)) {
            return -1L;
        }
        return keyStore.ttl(generateTokenKey(username, deviceType));
    }

    /**
     * FIXME:
     * get username from token
     *
     * @param tokenValue
     * @return
     */
    public String getUsername(String tokenValue) {
        String usernameWithId = getUserKeyFromTokenValue(tokenValue);
        return usernameWithId.split(slardarProperties.getToken().getSeparator())[0];
    }

    /**
     * 考虑同端策略
     *
     * @param username         用户名
     * @param deviceType       登录的设备类型
     * @param concurrentPolicy 登录策略
     * @return
     */
    public SlardarTokenProvider.SlardarToken createToken(@NonNull String username, @NonNull LoginDeviceType deviceType, @NonNull LoginConcurrentPolicy concurrentPolicy) {
        switch (concurrentPolicy) {
            case mutex:
                // 移除同端的token 重新生成
                removeUserAllTokens(username, deviceType);
                break;
            case share:
                SlardarTokenProvider.SlardarToken existedToken = getExistedToken(username, deviceType);
                if (StringUtils.hasText(existedToken.getTokenValue())) {
                    return existedToken;
                }
                break;
            case separate:
            default:
                break;
        }
        String id = simpleUUID();
        // 这里的 userKey 是 username + id 并不是, 原生的 username
        String userKey = keyJoiner.join(username, id);
        SlardarTokenProvider.SlardarToken slardarToken = getTokenImpl().provide(userKey);
        // TODO: into store
        // FIXME: 有效期也返回给客户端 用户缓存等
        setCommands.sadd(username, id);
//        stringCommands.setex(generateTokenKey(userKey, deviceType), Duration.between(LocalDateTime.now(), slardarToken.getExpiresAt()).getSeconds(),
//                slardarToken.getTokenValue());
        keyStore.setex(generateTokenKey(userKey, deviceType), slardarToken.getTokenValue(),
                Duration.between(LocalDateTime.now(), slardarToken.getExpiresAt()).getSeconds());
        return slardarToken;
    }


    /**
     * TODO: 注销特定设备的 指定 token
     *
     * @param tokenValue 指定的 token 值
     * @param deviceType 操作设备
     * @return success  or fail
     */
    public boolean withdrawToken(String tokenValue, LoginDeviceType deviceType) {
        String userKeyFromTokenValue = getUserKeyFromTokenValue(tokenValue);
        String tokenKey = generateTokenKey(userKeyFromTokenValue, deviceType);
        if (keyStore.has(tokenKey)) {
            keyStore.remove(tokenKey);
            // TODO: 根据不同策略 进行不同处理
        } else {
            log.warn("相关token已失效");
        }
        return true;
    }


    /**
     * 刷新 token 替换原先的 token
     * 客户端需要更新 token 值
     *
     * @param tokenValue
     * @return
     */
    public SlardarTokenProvider.SlardarToken refreshToken(String tokenValue, LoginDeviceType deviceType) {
        String username = getUserKeyFromTokenValue(tokenValue);
        String key = generateTokenKey(username, deviceType);
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
        String username = getUserKeyFromTokenValue(tokenValue);
        String key = generateTokenKey(username, deviceType);
        String existedToken = getFromRedis(key);
        if (StringUtils.isEmpty(existedToken)) {
            log.error("[authz] 续期失败, key 为 [{}] 的token 不存在", key);
            return false;
        }
        // TODO: 需要转移到具体实现类里
        stringCommands.setex(key, getTokenImpl().getTokenTTL(), existedToken);
        return true;
    }


    /**
     * TODO：
     * 注销同设备 同账号 所有 tokens
     * 存在问题 需要根据当前设置的同步策略 来区别处理
     *
     * @param username
     * @param deviceType
     * @return
     */
    public boolean removeUserAllTokens(@NonNull String username, @NonNull LoginDeviceType deviceType) {
        Set<String> ids = setCommands.smembers(username);
        if (ids == null) {
            return true;
        }
        if (ids.isEmpty()) {
            return true;
        }
        return ids.stream().allMatch(id -> stringCommands.del(generateTokenKey(username + slardarProperties.getToken().getSeparator() + id, deviceType)) != null &&
                setCommands.srem(username, id) != null);
    }

    /**
     * 从 servlet 中获取到 token 值
     *
     * @param request
     * @return 不含前缀
     */
    public String getTokenValueFromServlet(HttpServletRequest request) {
        String tokenKey = slardarProperties.getToken().getKey();
        String tokenValue;
        // 按优先级依次尝试从不同位置获取 Token
        if (request.getAttribute(tokenKey) != null) {
            //   尝试从request attributes里读取
            tokenValue = String.valueOf(request.getAttribute(tokenKey));
        } else if (request.getParameter(tokenKey) != null) {
            // 尝试从请求体里面读取
            tokenValue = request.getParameter(tokenKey);
        } else if (request.getHeader(tokenKey) != null) {
            // 尝试从header里读取
            tokenValue = request.getHeader(tokenKey);
        } else {
            // 尝试从cookie里读取
            tokenValue = HttpServletUtil.getCookieValue(request, tokenKey);
        }
        // 如果 Token 值以 "Bearer " 开头，则去掉前缀
        if (tokenValue != null && tokenValue.startsWith(BEARER)) {
            tokenValue = tokenValue.replace(BEARER, "");
        }
        return tokenValue;
    }

    /**
     * 注入 token value 到 servlet
     *
     * @param tokenValue token 值
     */
    public void setTokenValueIntoServlet(String tokenValue, @NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        if (StringUtil.isBlank(tokenValue)) {
            return;
        }
        String tokenKey = slardarProperties.getToken().getKey();
        // 1. 将 Token 保存到请求属性中
        request.setAttribute(tokenKey, tokenValue);
        // 2. 将 Token 写入 Cookie  从配置中获取有效期
        int cookieMaxAge = slardarProperties.getToken().getTtl().intValue();
        HttpServletUtil.setCookie(response, tokenKey, tokenValue, cookieMaxAge, "", "", "Strict", true);
        // 3. 将 Token 写入到响应头里
        response.setHeader(tokenKey, tokenValue);
        response.addHeader(ACCESS_CONTROL_EXPOSE_HEADERS, tokenKey);
    }

    /**
     * 获取默认的结果处理
     *
     * @return
     */
    public SlardarAuthenticateResultAdapter getAuthResultHandler() {
        String resultHandlerType = slardarProperties.getLogin().getResultHandlerType();
        return spiFactory.findAuthenticateResultHandler(StringUtil.isBlank(resultHandlerType) ? SlardarDefaultAuthenticateResultAdapter.NAME : resultHandlerType);
    }

    /**
     * 从 token value 获取 userKey
     *
     * @param tokenValue
     * @return user01111
     */
    private String getUserKeyFromTokenValue(String tokenValue) {
        return getTokenImpl().geUserKey(tokenValue);
    }

    /**
     * 从存储中获取已存在的 token 值
     *
     * @param username
     * @param deviceType
     * @return 返回 空 表示不存在 或 已过期
     */
    private SlardarTokenProvider.SlardarToken getExistedToken(String username, LoginDeviceType deviceType) {
        // 取第一个以 username_xx 为key的值
        Set<String> ids = setCommands.smembers(username);
        String redisKey = "";
        if (ids != null && !ids.isEmpty()) {
            redisKey = generateTokenKey(keyJoiner.join(username, ids.iterator().next()), deviceType);
        } else {
            redisKey = generateTokenKey(username, deviceType);
        }
        // 返回剩余秒数
        Long remainSeconds = stringCommands.ttl(redisKey);
        String tokenValue = getFromRedis(redisKey);
        return new SlardarTokenProvider.SlardarToken().setTokenValue(tokenValue).setExpiresAt(LocalDateTime.now().plusSeconds(remainSeconds));
    }

    /**
     * 根据用户名和设备 生成 token 的key
     *
     * @param userKey    用户名
     * @param deviceType 设备类型
     * @return eg: `slardar_token_PC_user012222`
     */
    private String generateTokenKey(String userKey, LoginDeviceType deviceType) {
        return keyJoiner.join(TOKEN_KEY_PREFIX, deviceType.name(), userKey);
    }

    /**
     * 简化的UUID，去掉了横线
     *
     * @return 简化的UUID，去掉了横线
     */
    private String simpleUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }


    private String getFromRedis(String key) {
        return stringCommands.get(key);
    }

    private boolean hasFromRedis(String key) {
        return stringCommands.exists(key) > 0;
    }


    private SlardarTokenProvider getTokenImpl() {
        return spiFactory.findTokenProvider(this.slardarProperties.getToken().getType());
    }

}
