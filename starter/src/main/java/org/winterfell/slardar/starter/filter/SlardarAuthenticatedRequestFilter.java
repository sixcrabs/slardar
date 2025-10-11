package org.winterfell.slardar.starter.filter;

import cn.piesat.v.misc.hutool.mini.StringUtil;
import org.winterfell.slardar.core.SlardarException;
import org.winterfell.slardar.core.entity.Account;
import org.winterfell.slardar.spi.SlardarSpiContext;
import org.winterfell.slardar.starter.SlardarEventManager;
import org.winterfell.slardar.starter.SlardarAuthenticateService;
import org.winterfell.slardar.starter.SlardarUserDetails;
import org.winterfell.slardar.starter.authenticate.SlardarAuthentication;
import org.winterfell.slardar.starter.config.SlardarProperties;
import org.winterfell.slardar.starter.support.LoginDeviceType;
import org.winterfell.slardar.starter.support.SecUtil;
import org.winterfell.slardar.starter.support.event.LogoutEvent;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
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

import static org.winterfell.slardar.core.Constants.AUTH_LOGOUT_URL;
import static org.winterfell.slardar.core.Constants.AUTH_USER_DETAILS_URL;
import static org.winterfell.slardar.starter.support.HttpServletUtil.*;

/**
 * <p>
 * 处理登录后的请求
 * <strong>前提是必须登录</strong>
 * <ul>
 *     <li>/userdetails 用户详细信息</li>
 *     <li>/logout   登出</li>
 * </ul>
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/13
 */
public class SlardarAuthenticatedRequestFilter extends GenericFilterBean {

    private final List<RequestMatcher> requestMatchers;

    private final SlardarSpiContext context;

    @Autowired
    private SlardarAuthenticateService authenticateService;

    @Autowired
    private UserDetailsService userDetailsService;

    public SlardarAuthenticatedRequestFilter(SlardarProperties properties, SlardarSpiContext context) {
        this.context = context;
        this.requestMatchers = Lists.newArrayList(new AntPathRequestMatcher(AUTH_USER_DETAILS_URL, HttpMethod.POST.name()),
                new AntPathRequestMatcher(AUTH_LOGOUT_URL, HttpMethod.POST.name()));
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (!requestMatches(request)) {
            chain.doFilter(request, response);
        } else {
            String uri = request.getRequestURI();
            if (uri.equalsIgnoreCase(AUTH_USER_DETAILS_URL)) {
                handleUserDetails(request, response);
            } else if (uri.equals(AUTH_LOGOUT_URL)) {
                handleLogout(request, response);
            }
        }
    }

    /**
     * 处理获取用户详情请求
     *
     * @param request
     * @param response
     * @throws IOException
     */
    private void handleUserDetails(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SlardarUserDetails userDetails = (SlardarUserDetails) SecUtil.getUserDetails();
        Account account = userDetails.getAccount();
        Account cloned = new Account();
        if (account != null) {
            cloned = account.clone();
            cloned.setPassword("");
        }
        sendJsonOk(response, makeSuccessResult(new SlardarUserDetails(cloned)));
    }


    /**
     * 处理登出请求
     *
     * @param request
     * @param response
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) {
        String tokenValue = authenticateService.getTokenValueFromServlet(request);
        if (StringUtil.isBlank(tokenValue)) {
            sendJson(response, makeErrorResult("Not logged in", HttpStatus.UNAUTHORIZED.value()), HttpStatus.UNAUTHORIZED, request.getHeader("Origin"));
        }
        LoginDeviceType deviceType = getDeviceType(request);
        if (authenticateService.isExpired(tokenValue, deviceType)) {
            sendJson(response, makeErrorResult("Token has been expired", HttpStatus.UNAUTHORIZED.value()), HttpStatus.UNAUTHORIZED, request.getHeader("Origin"));
        }
        String username = authenticateService.getUsernameFromTokenValue(tokenValue);
        SlardarUserDetails userDetails = (SlardarUserDetails) userDetailsService.loadUserByUsername(username);
        Account account = userDetails.getAccount();
        boolean b = authenticateService.withdrawToken(tokenValue, deviceType);
        if (b) {
            SlardarAuthentication logoutAuth = new SlardarAuthentication(username, "", null);
            logoutAuth.setAuthenticated(false);
            sendJsonOk(response, makeSuccessResult("Logout Successful"));
            try {
                context.getBeanIfAvailable(SlardarEventManager.class).dispatch(new LogoutEvent(account, request));
            } catch (SlardarException e) {
                throw new RuntimeException(e);
            }
        } else {
            sendJson(response, makeErrorResult("Server error...", HttpStatus.EXPECTATION_FAILED.value()), HttpStatus.EXPECTATION_FAILED, request.getHeader("Origin"));
        }
    }

    private boolean requestMatches(HttpServletRequest request) {
        return this.requestMatchers.stream().anyMatch(requestMatcher -> requestMatcher.matches(request));
    }
}
