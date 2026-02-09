package io.github.sixcrabs.slardar.starter.authenticate;

import com.google.common.collect.Lists;
import io.github.sixcrabs.slardar.spi.SlardarKeyStore;
import io.github.sixcrabs.slardar.spi.SlardarSpiFactory;
import io.github.sixcrabs.slardar.spi.authenticate.SlardarAuthenticateResultAdapter;
import io.github.sixcrabs.slardar.spi.token.SlardarTokenProvider;
import io.github.sixcrabs.slardar.starter.SlardarProperties;
import io.github.sixcrabs.slardar.starter.handler.SlardarDefaultAuthenticateResultAdapter;
import io.github.sixcrabs.slardar.starter.support.HttpServletUtil;
import io.github.sixcrabs.slardar.starter.support.LoginConcurrentPolicy;
import io.github.sixcrabs.slardar.starter.support.LoginDeviceType;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import io.github.sixcrabs.winterfell.mini.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static io.github.sixcrabs.slardar.core.Constants.BEARER;
import static io.github.sixcrabs.slardar.core.Constants.KEY_PREFIX_TOKEN;
import static io.github.sixcrabs.slardar.starter.support.LoginConcurrentPolicy.mutex;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static io.github.sixcrabs.slardar.starter.support.LoginDeviceType.APP;
import static io.github.sixcrabs.slardar.starter.support.LoginDeviceType.PC;


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

    private final Joiner keyJoiner;

    private final Splitter keySplitter;

    public static final Logger log = LoggerFactory.getLogger(SlardarAuthenticateService.class);

    private final SlardarKeyStore keyStore;

    public SlardarAuthenticateService(SlardarProperties slardarProperties, SlardarSpiFactory spiFactory) {
        this.slardarProperties = slardarProperties;
        this.spiFactory = spiFactory;
        this.keyStore = spiFactory.findKeyStore(slardarProperties.getKeyStore().getType());
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
     * get username from token
     *
     * @param tokenValue
     * @return
     */
    public String getUsernameFromTokenValue(String tokenValue) {
        String userKey = getUserKeyFromTokenValue(tokenValue);
        return keySplitter.splitToList(userKey).get(0);
    }

    /**
     * 考虑同端策略
     *
     * @param username   用户名
     * @param deviceType 登录的设备类型
     * @return
     */
    public SlardarTokenProvider.SlardarToken createToken(@NonNull String username, @NonNull LoginDeviceType deviceType) {
        LoginConcurrentPolicy concurrentPolicy = slardarProperties.getLogin().getConcurrentPolicy();
        if (mutex.equals(concurrentPolicy)) {
            // 互斥策略时，先失效所有的已经存在的同端的token
            withdrawTokensByUserAndDevice(username, deviceType);
        }
        String id = simpleUUID();
        // 这里的 userKey 是 username + id,  并不是原生的 username
        String userKey = keyJoiner.join(username, id);
        SlardarTokenProvider.SlardarToken slardarToken = getTokenImpl().provide(userKey);
        keyStore.setex(generateTokenKey(userKey, deviceType), slardarToken.getTokenValue(), Duration.between(LocalDateTime.now(), slardarToken.getExpiresAt()).getSeconds());
        return slardarToken;
    }


    /**
     * 注销特定设备的 指定 token 用于 登出
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
        } else {
            log.warn("相关token已失效");
        }
        return true;
    }

    /**
     * 注销所有 token （多端）
     * @param tokenValue
     * @return
     */
    public boolean withdrawToken(String tokenValue) {
        Lists.newArrayList(PC, APP).forEach(deviceType -> withdrawToken(tokenValue, deviceType));
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
        String tokenKey = generateTokenKey(getUserKeyFromTokenValue(tokenValue), deviceType);
        if (keyStore.has(tokenKey)) {
            keyStore.remove(tokenKey);
        }
        String username = getUsernameFromTokenValue(tokenValue);
        return createToken(username, deviceType);
    }

    /**
     * 同一个 token 续期, 延长过期 但不会更新 token值
     *
     * @param tokenValue
     * @param deviceType
     */
    public void renewToken(String tokenValue, LoginDeviceType deviceType) {
        String username = getUserKeyFromTokenValue(tokenValue);
        String key = generateTokenKey(username, deviceType);
        String existedToken = keyStore.get(key);
        if (!StringUtils.hasText(existedToken)) {
            log.error("token 续期失败, key 为 [{}] 的token 不存在", key);
            return;
        }
        keyStore.setex(key, existedToken, getTokenImpl().getTokenTTL());
    }


    /**
     * 注销同设备 同账号 所有 tokens
     *
     * @param username   用户名
     * @param deviceType 设备类型
     */
    public void withdrawTokensByUserAndDevice(@NonNull String username, @NonNull LoginDeviceType deviceType) {
        String prefix = keyJoiner.join(KEY_PREFIX_TOKEN, deviceType.name(), username);
        List<String> keysSelected = keyStore.keys(prefix);
        keysSelected.forEach(keyStore::remove);
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
    public void setTokenValueIntoServlet(String tokenValue, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
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
     * 根据用户名和设备 生成 token 的key
     *
     * @param userKey    用户名
     * @param deviceType 设备类型
     * @return eg: `slardar_token_PC_user_012222`
     */
    private String generateTokenKey(String userKey, LoginDeviceType deviceType) {
        return keyJoiner.join(KEY_PREFIX_TOKEN, deviceType.name(), userKey);
    }

    /**
     * 简化的UUID，去掉了横线
     *
     * @return 简化的UUID，去掉了横线
     */
    private String simpleUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private SlardarTokenProvider getTokenImpl() {
        return spiFactory.findTokenProvider(this.slardarProperties.getToken().getType());
    }

}