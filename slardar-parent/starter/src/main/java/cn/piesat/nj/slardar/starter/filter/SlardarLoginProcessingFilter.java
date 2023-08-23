package cn.piesat.nj.slardar.starter.filter;

import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.authenticate.handler.SlardarAuthenticateHandler;
import cn.piesat.nj.slardar.starter.authenticate.handler.SlardarAuthenticateHandlerFactory;
import cn.piesat.nj.slardar.starter.config.SlardarProperties;
import cn.piesat.nj.slardar.starter.support.LoginDeviceType;
import cn.piesat.nj.slardar.starter.authenticate.SlardarAuthentication;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static cn.piesat.nj.slardar.core.Constants.HEADER_KEY_OF_AUTH_TYPE;
import static cn.piesat.nj.slardar.starter.support.HttpServletUtil.*;

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
public class SlardarLoginProcessingFilter extends AbstractAuthenticationProcessingFilter {

    private final SlardarAuthenticateHandlerFactory authenticateHandlerFactory;

    private boolean postOnly = true;

    public static final Gson GSON = new GsonBuilder().create();

    public SlardarLoginProcessingFilter(SlardarProperties securityProperties,
                                        AuthenticationManager authenticationManager,
                                        AuthenticationFailureHandler authenticationFailureHandler,
                                        AuthenticationSuccessHandler authenticationSuccessHandler,
                                        SlardarAuthenticateHandlerFactory authenticateHandlerFactory) {
        super(new AntPathRequestMatcher(securityProperties.getLogin().getUrl()));
        this.postOnly = securityProperties.getLogin().isPostOnly();
        this.authenticateHandlerFactory = authenticateHandlerFactory;
        setAuthenticationManager(authenticationManager);
        setAuthenticationSuccessHandler(authenticationSuccessHandler);
        setAuthenticationFailureHandler(authenticationFailureHandler);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        // 从请求中取出认证需要的信息 组装成 auth token
        if (authenticateHandlerFactory != null) {
            if (this.postOnly && !HttpMethod.POST.matches(request.getMethod())) {
                throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
            }
            try {
                Map<String, String> requestParam = getRequestParam(request);
                if (requestParam.isEmpty()) {
                    requestParam = GSON.fromJson(getRequestPostStr(request), Map.class);
                }
                if (Objects.isNull(requestParam)) {
                    throw new AuthenticationServiceException("Login params cannot be null");
                }
                Map<String, String> requestHeaders = getHeaders(request);
                // 1. 找出匹配的认证处理器 根据请求头参数： X-Auth-Type
                // 2. 调用接口 将请求解析为 authentication 对象
                // 3. 委托给 manager 去实现
                SlardarAuthenticateHandler authenticateHandler = authenticateHandlerFactory.findAuthenticateHandler(requestHeaders.get(HEADER_KEY_OF_AUTH_TYPE));
                SlardarAuthentication authenticationToken = authenticateHandler.handleRequest(new RequestWrapper()
                        .setRequestParams(requestParam)
                        .setLoginDeviceType(getDeviceType(request))
                        .setSessionId(getSessionId(request))
                        .setRequestHeaders(requestHeaders));
                // 调用特定的认证逻辑
                return authenticateHandler.doAuthenticate(authenticationToken);
            } catch (AuthenticationServiceException e) {
                throw e;
            } catch (SlardarException e) {
                throw new AuthenticationServiceException(e.getLocalizedMessage());
            }
        } else {
            throw new AuthenticationServiceException("must implements interface `AuthenticationHandler`");
        }
    }

    public static class RequestWrapper {

        private Map<String, String> requestParams;

        private String sessionId;

        private Map<String, String> requestHeaders;

        private LoginDeviceType loginDeviceType;

        public Map<String, String> getRequestParams() {
            return requestParams;
        }

        public RequestWrapper setRequestParams(Map<String, String> requestParams) {
            this.requestParams = requestParams;
            return this;
        }

        public LoginDeviceType getLoginDeviceType() {
            return loginDeviceType;
        }

        public RequestWrapper setLoginDeviceType(LoginDeviceType loginDeviceType) {
            this.loginDeviceType = loginDeviceType;
            return this;
        }

        public String getSessionId() {
            return sessionId;
        }

        public RequestWrapper setSessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Map<String, String> getRequestHeaders() {
            return requestHeaders;
        }

        public RequestWrapper setRequestHeaders(Map<String, String> requestHeaders) {
            this.requestHeaders = requestHeaders;
            return this;
        }
    }


}
