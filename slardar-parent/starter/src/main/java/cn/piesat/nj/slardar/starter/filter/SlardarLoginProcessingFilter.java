package cn.piesat.nj.slardar.starter.filter;

import cn.piesat.nj.slardar.starter.config.SlardarProperties;
import cn.piesat.nj.slardar.starter.handler.authentication.AuthenticationRequestHandlerFactory;
import cn.piesat.v.authx.security.infrastructure.spring.AuthxAuthenticationRequestHandler;
import cn.piesat.v.authx.security.infrastructure.spring.SecurityProperties;
import cn.piesat.v.authx.security.infrastructure.spring.handler.authentication.AuthxAuthenticationRequestHandlerFactory;
import cn.piesat.v.authx.security.infrastructure.spring.support.AuthxAuthentication;
import cn.piesat.v.authx.security.infrastructure.spring.support.AuthxRequestWrapper;
import cn.piesat.v.authx.security.infrastructure.spring.support.SecUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

import static cn.piesat.v.authx.security.infrastructure.spring.support.SecUtil.*;

/**
 * <p>
 * 处理身份认证的 filter
 *  /login
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/26
 */
public class SlardarLoginProcessingFilter extends AbstractAuthenticationProcessingFilter {

    private final AuthenticationRequestHandlerFactory requestHandlerFactory;

    private boolean postOnly = true;

    public static final Gson GSON = new GsonBuilder().create();

    public SlardarLoginProcessingFilter(SlardarProperties securityProperties,
                                        AuthenticationManager authenticationManager,
                                        AuthenticationFailureHandler authenticationFailureHandler,
                                        AuthenticationSuccessHandler authenticationSuccessHandler,
                                        AuthxAuthenticationRequestHandlerFactory requestHandlerFactory) {
        super(new AntPathRequestMatcher(securityProperties.getLogin().getUrl(), "POST"));
        this.requestHandlerFactory = requestHandlerFactory;
        setAuthenticationManager(authenticationManager);
        setAuthenticationSuccessHandler(authenticationSuccessHandler);
        setAuthenticationFailureHandler(authenticationFailureHandler);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        // 从请求中取出认证需要的信息 组装成 auth token
        if (requestHandlerFactory != null) {
            if (this.postOnly && !"POST".equals(request.getMethod())) {
                throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
            }
            try {
                Map<String, String> requestParam = getRequestParam(request);
                if (requestParam.isEmpty()) {
                    requestParam = GSON.fromJson(SecUtil.getRequestPostStr(request), Map.class);
                }
                Map<String, String> requestHeaders = getHeaders(request);
                // 找出匹配的认证处理器
                AuthxAuthenticationRequestHandler requestHandler = requestHandlerFactory.findRequestHandler(requestHeaders.get(AUTH_TYPE_HEADER_KEY));
                AuthxAuthentication authentication = requestHandler.handle(new AuthxRequestWrapper()
                        .setRequestParams(requestParam)
                        .setSessionId(request.getSession() != null ? request.getSession().getId() : "")
                        .setRequestHeaders(requestHeaders));
                // 调用自定义实现的 provider 去实现特定的认证逻辑
                // @see SlardarAuthenticationProvider
                return this.getAuthenticationManager().authenticate(authentication);
            } catch (AuthenticationServiceException e) {
                throw e;
            }
        } else {
            throw new AuthenticationServiceException("must implements interface `AuthenticationRequestHandler`");
        }
    }


}
