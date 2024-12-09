package cn.piesat.v.slardar.sso.server;

import cn.piesat.v.slardar.sso.server.config.SsoServerProperties;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 * .TODO
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
        this.requestMatcher = new AntPathRequestMatcher(properties.getSsoAntUrlPattern());
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
