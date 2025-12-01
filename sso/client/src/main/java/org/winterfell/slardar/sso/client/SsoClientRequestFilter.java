package org.winterfell.slardar.sso.client;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.winterfell.slardar.sso.client.config.SsoClientProperties;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

/**
 * <p>
 * .
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/30
 */
public class SsoClientRequestFilter extends GenericFilterBean {

    private final SsoClientRequestHandler requestHandler;

    private final SsoClientProperties clientProperties;

    public SsoClientRequestFilter(SsoClientRequestHandler requestHandler, SsoClientProperties clientProperties) {
        this.requestHandler = requestHandler;
        this.clientProperties = clientProperties;
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
        String uri = request.getRequestURI();
        return uri.startsWith(clientProperties.getCtxPath());
    }
}