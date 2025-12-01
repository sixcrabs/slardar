package org.winterfell.slardar.starter.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.winterfell.slardar.core.SlardarException;
import org.winterfell.slardar.starter.authenticate.handler.SlardarAuthenticateHandler;
import org.winterfell.slardar.starter.authenticate.handler.SlardarAuthenticateHandlerFactory;
import org.winterfell.slardar.starter.SlardarProperties;
import org.winterfell.slardar.starter.support.HttpServletUtil;
import org.winterfell.slardar.starter.authenticate.SlardarAuthentication;
import org.winterfell.slardar.starter.support.RequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Map;

import static org.winterfell.slardar.core.Constants.HEADER_KEYS_OF_AUTH_TYPE;
import static org.winterfell.slardar.starter.support.HttpServletUtil.*;

/**
 * <p>
 * 处理身份认证的 filter
 * <p>
 * /login
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/26
 */
public class SlardarLoginFilter extends AbstractAuthenticationProcessingFilter {

    private final SlardarAuthenticateHandlerFactory authenticateHandlerFactory;

    private final boolean postOnly;

    private static final Logger logger = LoggerFactory.getLogger(SlardarLoginFilter.class);

    public SlardarLoginFilter(SlardarProperties securityProperties,
                              AuthenticationFailureHandler authenticationFailureHandler,
                              AuthenticationSuccessHandler authenticationSuccessHandler,
                              SlardarAuthenticateHandlerFactory authenticateHandlerFactory) {
        super(new AntPathRequestMatcher(securityProperties.getLogin().getUrl()));
        this.postOnly = securityProperties.getLogin().isPostOnly();
        this.authenticateHandlerFactory = authenticateHandlerFactory;
        setAuthenticationManager(authentication -> null);
        setAuthenticationSuccessHandler(authenticationSuccessHandler);
        setAuthenticationFailureHandler(authenticationFailureHandler);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
        if (authenticateHandlerFactory != null) {
            if (this.postOnly && !HttpMethod.POST.matches(request.getMethod())) {
                throw new AuthenticationServiceException("Authentication method [" + request.getMethod() + "] not supported. ");
            }
            try {
                Map<String, String> requestParams = getRequestParamWithPostStr(request);
                if (requestParams.isEmpty()) {
                    throw new AuthenticationServiceException("Login params cannot be empty");
                }
                Map<String, String> requestHeaders = getHeaders(request);
                // 1. 找出匹配的认证处理器 根据请求头参数： X-Auth-Type
                // 2. 调用接口 将请求解析为 authentication 对象
                // 3. 交给对应的handler去认证
                String authType = getAuthType(request);
                SlardarAuthenticateHandler authenticateHandler = authenticateHandlerFactory.findAuthenticateHandler(authType);
                if (authenticateHandler == null) {
                    logger.warn("No handler found for auth type: {}", authType);
                    throw new AuthenticationServiceException("Unsupported authentication type: " + authType);
                }
                SlardarAuthentication authenticationToken = authenticateHandler.handleRequest(new RequestWrapper()
                        .setRequestParams(requestParams)
                        .setLoginDeviceType(getDeviceType(request))
                        .setSessionId(getSessionId(request))
                        .setRequestHeaders(requestHeaders));
                authenticationToken.setReqClientIp(HttpServletUtil.getRequestIpAddress(request));
                // 调用特定的认证逻辑
                return authenticateHandler.doAuthenticate(authenticationToken);
            } catch (SlardarException | IOException e) {
                logger.error("Authentication failed: {}", e.getMessage());
                throw new AuthenticationServiceException(e.getLocalizedMessage());
            }
        } else {
            throw new AuthenticationServiceException("must implements interface `AuthenticationHandler`");
        }
    }

    /**
     * 兼容 X-Auth-Type 大小写等多种情况
     *
     * @param request
     * @return
     */
    private String getAuthType(final HttpServletRequest request) {
        return HEADER_KEYS_OF_AUTH_TYPE.stream().filter(headerKey -> request.getHeader(headerKey) != null).findFirst().orElse(null);
    }

}