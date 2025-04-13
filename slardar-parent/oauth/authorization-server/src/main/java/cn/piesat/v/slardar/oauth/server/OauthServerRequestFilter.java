package cn.piesat.v.slardar.oauth.server;

import cn.piesat.v.slardar.oauth.server.config.OauthServerProperties;
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
 * TODO:
 * 处理 /oauth/... 请求
 * </p>
 *
 * @author Alex
 * @since 2025/4/11
 */
public class OauthServerRequestFilter extends GenericFilterBean {

    private final RequestMatcher requestMatcher;

    private final OauthServerRequestHandler requestHandler;

    public OauthServerRequestFilter(OauthServerProperties properties, OauthServerRequestHandler requestHandler) {
        this.requestMatcher = new AntPathRequestMatcher(properties.getOauthAntUrlPattern());
        this.requestHandler = requestHandler;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (requestMatches(request)) {
            requestHandler.handle(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean requestMatches(HttpServletRequest request) {
        return this.requestMatcher.matches(request);
    }
}
