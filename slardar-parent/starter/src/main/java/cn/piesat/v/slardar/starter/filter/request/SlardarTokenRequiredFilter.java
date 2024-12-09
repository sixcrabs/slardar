package cn.piesat.v.slardar.starter.filter.request;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.core.SlardarSecurityHelper;
import cn.piesat.v.slardar.core.annotation.SlardarIgnore;
import cn.piesat.v.slardar.starter.SlardarAuthenticateService;
import cn.piesat.v.slardar.starter.SlardarUserDetails;
import cn.piesat.v.slardar.starter.support.LoginDeviceType;
import cn.piesat.v.slardar.starter.authenticate.SlardarAuthentication;
import cn.piesat.v.misc.hutool.mini.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static cn.piesat.v.slardar.starter.support.HttpServletUtil.*;

/**
 * 处理 token 验证
 * 拦截所有请求
 *
 * @author JiajieZhang
 * @date 2022/9/23
 * @description token过滤器
 */
public class SlardarTokenRequiredFilter extends OncePerRequestFilter {

    public static final Logger log = LoggerFactory.getLogger(SlardarTokenRequiredFilter.class);

    @Autowired
    private UserDetailsService userDetailsService;

    private static final ExecutorService POOL = new ThreadPoolExecutor(4, Runtime.getRuntime().availableProcessors() * 2,
            3000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(512), new NamedThreadFactory("auth-token-%d", true));

    @Resource
    private SlardarAuthenticateService tokenService;

    /**
     * 忽略的url pattern
     */
    private final String[] ignoredUrls;

    private final List<AntPathRequestMatcher> ignoredPathRequestMatchers = new ArrayList<>(1);


    public String[] getIgnoredUrls() {
        return ignoredUrls;
    }

    public SlardarTokenRequiredFilter(String[] ignoredUrls) {
        this.ignoredUrls = ignoredUrls;
        if (ignoredUrls != null && ignoredUrls.length > 0) {
            Arrays.stream(ignoredUrls).forEach(url -> ignoredPathRequestMatchers.add(new AntPathRequestMatcher(url)));
        }
    }

    /**
     * ignored url should not be filtered
     * Can be overridden in subclasses for custom filtering control,
     * returning {@code true} to avoid filtering of the given request.
     * <p>The default implementation always returns {@code false}.
     *
     * @param request current HTTP request
     * @return whether the given request should <i>not</i> be filtered
     * @throws ServletException in case of errors
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return ignoredPathRequestMatchers.stream().anyMatch(matcher -> matcher.matcher(request).isMatch());
    }

    /**
     * 外部添加过滤的url pattern
     *
     * @param antPattern
     * @param method
     * @see SlardarIgnore
     */
    public void addIgnoreUrlPattern(String antPattern, String method) {
        ignoredPathRequestMatchers.add(new AntPathRequestMatcher(antPattern, StringUtil.isBlank(method) ? null : method));
    }

    /**
     * 验证 token 代表的用户状态和信息
     *
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authToken = tokenService.getTokenValue(request);
        SlardarException tokenValidateEx = null;
        if (StringUtils.hasText(authToken)) {
            LoginDeviceType deviceType = null;
            String username = null;
            try {
                deviceType = getDeviceType(request);
                username = tokenService.getUsername(authToken);
            } catch (Exception e) {
                tokenValidateEx = new SlardarException(e.getLocalizedMessage());
            }
            if (Objects.isNull(tokenValidateEx) && !tokenService.isExpired(authToken, deviceType)) {
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 加载详细信息
                    SlardarUserDetails userDetails = (SlardarUserDetails) userDetailsService.loadUserByUsername(username);
                    if (userDetails.isEnabled()) {
                        SlardarAuthentication authenticationToken = new SlardarAuthentication(username, "", userDetails);
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        authenticationToken.setAuthenticated(true);
                        if (userDetails.getAccount() != null) {
                            SlardarSecurityHelper.getContext()
                                    .setAccount(userDetails.getAccount())
                                    .setUserProfile(userDetails.getAccount().getUserProfile())
                                    .setAuthenticated(true);
                        }
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        String finalAuthToken = authToken;
                        // TODO: 修改为事件
                        LoginDeviceType finalDeviceType = deviceType;
                        POOL.submit(() -> {
                            // token 续期
                            tokenService.renewToken(finalAuthToken, finalDeviceType);
                        });

                    } else {
                        // 账户过期
                        tokenValidateEx = new SlardarException("account has been expired or forbidden");
                    }
                }
            } else {
                tokenValidateEx = new SlardarException("token is not valid");
            }
        }
        if (tokenValidateEx != null) {
            forwardRequest(request, response, tokenValidateEx, "remoteLoginException", "/remoteLoginException");
        } else {
            filterChain.doFilter(request, response);
        }
    }

    /**
     * 转发
     *
     * @param request
     * @param response
     * @param e
     * @param param
     * @param url
     * @throws ServletException
     * @throws IOException
     */
    private void forwardRequest(HttpServletRequest request, HttpServletResponse response, SlardarException e, String param, String url) throws ServletException, IOException {
        request.setAttribute(param, e);
        sendJson(response, makeErrorResult(e.getLocalizedMessage(), HttpStatus.UNAUTHORIZED.value()), HttpStatus.UNAUTHORIZED, request.getHeader("Origin"));
//        response.getWriter().println(GSON.toJson(ImmutableMap.of("code", HttpStatus.UNAUTHORIZED.value(), "message", e.getLocalizedMessage())));
//        request.getRequestDispatcher(url).forward(request, response);
    }
}