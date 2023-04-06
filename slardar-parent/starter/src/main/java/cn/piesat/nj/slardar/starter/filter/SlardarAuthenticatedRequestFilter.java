package cn.piesat.nj.slardar.starter.filter;

import cn.hutool.core.map.MapUtil;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.core.entity.Account;
import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.SlardarTokenService;
import cn.piesat.nj.slardar.starter.SlardarUserDetails;
import cn.piesat.nj.slardar.starter.config.SlardarProperties;
import cn.piesat.nj.slardar.starter.support.LoginDeviceType;
import cn.piesat.nj.slardar.starter.support.SecUtil;
import cn.piesat.nj.slardar.starter.support.event.LogoutEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static cn.piesat.nj.slardar.core.Constants.AUTH_LOGOUT_URL;
import static cn.piesat.nj.slardar.core.Constants.AUTH_USER_DETAILS_URL;
import static cn.piesat.nj.slardar.starter.support.HttpServletUtil.isFromMobile;
import static cn.piesat.nj.slardar.starter.support.SecUtil.GSON;
import static cn.piesat.nj.slardar.starter.support.SecUtil.objectMapper;

/**
 * <p>
 * 处理登录后的请求
 * 前提是必须登录
 * - /userdetails 用户详细信息
 * - /logout   登出
 * - ...
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/13
 */
public class SlardarAuthenticatedRequestFilter extends GenericFilterBean {

    private final List<RequestMatcher> requestMatchers;

    private final SlardarContext context;

    @Autowired
    private SlardarTokenService tokenService;

    public SlardarAuthenticatedRequestFilter(SlardarProperties properties, SlardarContext context) {
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
                String currentUsername = SecUtil.getCurrentUsername();
                boolean b = tokenService.removeTokens(currentUsername, isFromMobile(request) ? LoginDeviceType.APP : LoginDeviceType.PC);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                if (b) {
                    SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
                    response.setStatus(HttpStatus.OK.value());
                    response.getWriter().write(GSON.toJson(MapUtil.of("msg", "ok")));
                    try {
                        context.getEventManager().dispatch(new LogoutEvent(currentUsername));
                    } catch (SlardarException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                    response.getWriter().write(GSON.toJson(MapUtil.of("msg", "server error...")));
                }
            }
        }
    }

    private boolean requestMatches(HttpServletRequest request) {
        return this.requestMatchers.stream().anyMatch(requestMatcher -> requestMatcher.matches(request));
    }
}
