package cn.piesat.nj.slardar.starter.handler.authentication;

import cn.piesat.v.authx.security.infrastructure.spring.AuthxAuthenticationRequestHandler;

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
public class AuthxAuthenticationRequestHandlerFactory {

    private static final Map<String, AuthxAuthenticationRequestHandler> CACHE = new ConcurrentHashMap<>(1);

    @Resource
    private AuthxDefaultAuthenticationRequestHandler defaultAuthenticationRequestHandler;

    @Resource
    private AuthxWxAuthenticationRequestHandler wxAuthenticationRequestHandler;

    public AuthxAuthenticationRequestHandler findRequestHandler(String authType) {
        return CACHE.computeIfAbsent(authType, type -> {
            switch (authType) {
                case "wxapp":
                    return wxAuthenticationRequestHandler;
                case "password":
                default:
                    return defaultAuthenticationRequestHandler;

            }
        });
    }
}
