package io.github.sixcrabs.slardar.ext.firewall;

import io.github.sixcrabs.slardar.core.SlardarContext;
import io.github.sixcrabs.slardar.core.SlardarException;
import io.github.sixcrabs.slardar.starter.config.SlardarBeanConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import io.github.sixcrabs.slardar.ext.firewall.handlers.SlardarFirewallBlackPathsHandler;
import io.github.sixcrabs.slardar.ext.firewall.handlers.SlardarFirewallHeadersHandler;
import io.github.sixcrabs.slardar.ext.firewall.handlers.SlardarFirewallHostsHandler;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static io.github.sixcrabs.slardar.core.Constants.AUTH_LOGIN_URL;
import static io.github.sixcrabs.slardar.starter.support.HttpServletUtil.sendError;


/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/10/22
 */
public class SlardarFirewallFilter extends OncePerRequestFilter {

    private final SlardarContext context;

    private final List<AntPathRequestMatcher> ignoredPathRequestMatchers = new ArrayList<>(1);

    public SlardarFirewallFilter(SlardarContext context) {
        this.context = context;
        init();
    }

    private void init() {
        ignoredPathRequestMatchers.add(new AntPathRequestMatcher(AUTH_LOGIN_URL));
        Arrays.stream(SlardarBeanConfiguration.STATIC_RES_MATCHERS).forEach(url-> ignoredPathRequestMatchers.add(new AntPathRequestMatcher(url)));
        // 添加已有的防火墙handler 到容器中
        SlardarFirewallProperties properties = context.getBean(SlardarFirewallProperties.class);
        if(properties.getBlackPath().isEnabled()) {
            SlardarFirewallHandlerContainer.getInstance().addHandler(new SlardarFirewallBlackPathsHandler(properties.getBlackPath()));
        }
        if(properties.getHeaders().isEnabled()) {
            SlardarFirewallHandlerContainer.getInstance().addHandler(new SlardarFirewallHeadersHandler(properties.getHeaders()));
        }
        if(properties.getHosts().isEnabled()) {
            SlardarFirewallHandlerContainer.getInstance().addHandler(new SlardarFirewallHostsHandler(properties.getHosts()));
        }
        // 添加 spring 容器内的自定义实现
        Collection<SlardarFirewallHandler> handlers = context.getBeans(SlardarFirewallHandler.class);
        handlers.stream().filter(SlardarFirewallHandler::isEnabled).forEach(SlardarFirewallHandlerContainer.getInstance()::addHandler);
    }

    /**
     * 对于在slardar中忽略的 url 不进行防火墙功能过滤
     *
     * @param request current HTTP request
     * @return
     * @throws ServletException
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return this.ignoredPathRequestMatchers.stream().anyMatch(matcher -> matcher.matcher(request).isMatch());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            SlardarFirewallHandlerContainer.getInstance().execute(request, response, context);
            filterChain.doFilter(request, response);
        } catch (SlardarException e) {
            sendError(request, response, HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }
}