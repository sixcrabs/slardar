package org.winterfell.slardar.sso.server;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.winterfell.slardar.sso.server.config.SsoServerProperties;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

/**
 * <p>
 * 处理 /sso 请求的 filter
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
public class SsoServerRequestFilter extends GenericFilterBean {

    private final RequestMatcher requestMatcher;

    private final SsoServerRequestHandler requestHandler;

    public SsoServerRequestFilter(SsoServerProperties properties, SsoServerRequestHandler requestHandler) {
        this.requestMatcher = PathPatternRequestMatcher.withDefaults().matcher(properties.getSsoAntUrlPattern());
        this.requestHandler = requestHandler;
    }


    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (requestMatches(request)) {
            // 交给 handler 去处理
            requestHandler.handle(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean requestMatches(HttpServletRequest request) {
        return this.requestMatcher.matches(request);
    }
}