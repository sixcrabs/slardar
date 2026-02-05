package io.github.sixcrabs.slardar.license.manager;

import io.github.sixcrabs.slardar.starter.config.customizer.SlardarIgnoringCustomizer;
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
import java.util.List;

/**
 * <p>
 * 拦截 请求
 * </p>
 *
 * @author Alex
 * @since 2025/9/23
 */
public class LicenseManageRequestFilter extends GenericFilterBean implements SlardarIgnoringCustomizer {

    private final RequestMatcher requestMatcher;

    private final LicenseManageRequestHandler requestHandler;

    public static final String LIC_VIEW_URL = "/licenseManage";
    public static final String LIC_REQ_URL = "/license/**";

    public LicenseManageRequestFilter(LicenseManageRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
        this.requestMatcher = new AntPathRequestMatcher(LIC_REQ_URL);
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

    /**
     * 自定义过滤需要忽略的url
     *
     * @param antPatterns
     */
    @Override
    public void customize(List<String> antPatterns) {
        antPatterns.add(LIC_REQ_URL);
        antPatterns.add(LIC_VIEW_URL);
    }
}