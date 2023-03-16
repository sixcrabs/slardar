package cn.piesat.nj.slardar.starter.handler.authentication;

import cn.piesat.nj.slardar.core.Constants;
import cn.piesat.nj.slardar.starter.AuthenticationRequestHandler;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * 用于选择不同类型的handler 去处理认证请求
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/2/3
 */
public class AuthenticationRequestHandlerFactory {

    private static final Map<String, AuthenticationRequestHandler> CACHE = new ConcurrentHashMap<>(1);

    @Resource
    private DefaultAuthenticationRequestHandler defaultAuthenticationRequestHandler;

    @Resource
    private WxAuthenticationRequestHandler wxAuthenticationRequestHandler;

    public AuthenticationRequestHandler findRequestHandler(String authType) {
        if (StringUtils.isEmpty(authType)) {
            return defaultAuthenticationRequestHandler;
        }
        return CACHE.computeIfAbsent(authType, type -> {
            switch (authType) {
                case Constants.AUTH_TYPE_WX_APP:
                    return wxAuthenticationRequestHandler;
                case "password":
                default:
                    return defaultAuthenticationRequestHandler;

            }
        });
    }
}
