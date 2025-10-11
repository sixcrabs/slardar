package org.winterfell.slardar.sso.client;

import org.winterfell.slardar.sso.client.config.SsoClientProperties;
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
