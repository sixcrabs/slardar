package cn.piesat.nj.slardar.starter.filter;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.piesat.nj.slardar.core.entity.AuditLog;
import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.SlardarTokenService;
import cn.piesat.nj.slardar.starter.config.SlardarProperties;
import cn.piesat.nj.slardar.starter.support.LoginDeviceType;
import cn.piesat.nj.slardar.starter.support.SecUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.piesat.nj.slardar.starter.support.SecUtil.GSON;
import static cn.piesat.nj.slardar.starter.support.SecUtil.isFromMobile;

/**
 * <p>
 * 处理 前提是必须登录
 * /userdetails 请求
 * ...
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/13
 */
public class SlardarAuthenticatedRequestFilter extends GenericFilterBean {

    private final List<RequestMatcher> requestMatchers;

    private static ObjectMapper globalObjectMapper = new ObjectMapper();

    private final SlardarContext context;

    @Autowired
    private SlardarTokenService tokenService;

    public SlardarAuthenticatedRequestFilter(SlardarProperties properties, SlardarContext context) {
        this.context = context;
        this.requestMatchers = Lists.newArrayList(new AntPathRequestMatcher("/userdetail", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/logout", HttpMethod.POST.name()));
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (!requestMatches(request)) {
            chain.doFilter(request, response);
        } else {
            String uri = request.getRequestURI();
            if (uri.equalsIgnoreCase("/userdetail")) {
                // 详细用户对象
                Map<String, Object> details = new HashMap<>(1);

                // TODO 获取 token 验证、拿到 userdetails
                response.setStatus(HttpStatus.OK.value());
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                globalObjectMapper.writeValue(response.getWriter(), details);
                response.getWriter().flush();

            } else if (uri.equals("/logout")) {
                String currentUsername = SecUtil.getCurrentUsername();
                boolean b = tokenService.removeTokens(currentUsername, isFromMobile(request) ? LoginDeviceType.APP : LoginDeviceType.PC);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                if (b) {
                    SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
                    response.setStatus(HttpStatus.OK.value());
                    response.getWriter().write(GSON.toJson(MapUtil.of("msg", "ok")));
                    // TESTME: 写入 auditlog
                    ThreadUtil.newThread(() -> context.getAuditLogGateway().create(new AuditLog().setAccountName(currentUsername).setLogType("logout").setLogTime(LocalDateTime.now())), "logout-audit-thread");
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
