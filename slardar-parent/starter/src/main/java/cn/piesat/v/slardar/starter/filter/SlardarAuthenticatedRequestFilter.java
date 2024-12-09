package cn.piesat.v.slardar.starter.filter;

import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.core.entity.Account;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.starter.SlardarEventManager;
import cn.piesat.v.slardar.starter.SlardarAuthenticateService;
import cn.piesat.v.slardar.starter.SlardarUserDetails;
import cn.piesat.v.slardar.starter.config.SlardarProperties;
import cn.piesat.v.slardar.starter.support.LoginDeviceType;
import cn.piesat.v.slardar.starter.support.SecUtil;
import cn.piesat.v.slardar.starter.support.event.LogoutEvent;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.nio.charset.StandardCharsets;
import java.util.List;

import static cn.piesat.v.slardar.core.Constants.AUTH_LOGOUT_URL;
import static cn.piesat.v.slardar.core.Constants.AUTH_USER_DETAILS_URL;
import static cn.piesat.v.slardar.starter.support.HttpServletUtil.*;
import static cn.piesat.v.slardar.starter.support.SecUtil.objectMapper;

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
    private SlardarAuthenticateService tokenService;

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
                // 详细用户对象
                SlardarUserDetails userDetails = (SlardarUserDetails) SecUtil.getUserDetails();
                Account account = userDetails.getAccount();
                if (account != null) {
                    account.setPassword("");
                }
                response.setStatus(HttpStatus.OK.value());
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(response.getWriter(), userDetails);
                response.getWriter().flush();

            } else if (uri.equals(AUTH_LOGOUT_URL)) {
                boolean authenticated = SecUtil.isAuthenticated();
                if (!authenticated) {
                    sendJson(response, makeErrorResult("当前未登录", HttpStatus.UNAUTHORIZED.value()), HttpStatus.UNAUTHORIZED, request.getHeader("Origin"));
                }
                String currentUsername = SecUtil.getCurrentUsername();
                Account account = SecUtil.getAccount();
                boolean b = tokenService.removeTokens(currentUsername, isFromMobile(request) ? LoginDeviceType.APP : LoginDeviceType.PC);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                if (b) {
                    SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
                    sendJsonOk(response, makeSuccessResult(""));
                    try {
                        context.getBeanIfAvailable(SlardarEventManager.class).dispatch(new LogoutEvent(account, request));
                    } catch (SlardarException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    sendJson(response, makeErrorResult("server error...", HttpStatus.EXPECTATION_FAILED.value()), HttpStatus.EXPECTATION_FAILED, request.getHeader("Origin"));
                }
            }
        }
    }

    private boolean requestMatches(HttpServletRequest request) {
        return this.requestMatchers.stream().anyMatch(requestMatcher -> requestMatcher.matches(request));
    }
}
