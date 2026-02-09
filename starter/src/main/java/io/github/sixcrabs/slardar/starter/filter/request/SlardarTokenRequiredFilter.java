package io.github.sixcrabs.slardar.starter.filter.request;

import io.github.sixcrabs.slardar.core.SlardarException;
import io.github.sixcrabs.slardar.core.SlardarSecurityHelper;
import io.github.sixcrabs.slardar.core.annotation.SlardarIgnore;
import io.github.sixcrabs.slardar.starter.authenticate.SlardarAuthenticateService;
import io.github.sixcrabs.slardar.starter.authenticate.SlardarUserDetails;
import io.github.sixcrabs.slardar.starter.support.LoginDeviceType;
import io.github.sixcrabs.slardar.starter.authenticate.SlardarAuthentication;
import io.github.sixcrabs.winterfell.mini.StringUtil;
import io.github.sixcrabs.winterfell.mini.thread.NamedThreadFactory;
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

import javax.annotation.PreDestroy;
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

import static io.github.sixcrabs.slardar.core.Constants.*;
import static io.github.sixcrabs.slardar.starter.support.HttpServletUtil.*;

/**
 * 处理 token 验证
 * 拦截所有请求
 *
 * @author JiajieZhang
 */
public class SlardarTokenRequiredFilter extends OncePerRequestFilter {

    public static final Logger log = LoggerFactory.getLogger(SlardarTokenRequiredFilter.class);

    @Autowired
    private UserDetailsService userDetailsService;

    private static final ExecutorService POOL = new ThreadPoolExecutor(4, Runtime.getRuntime().availableProcessors() * 2,
            3000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(512), new NamedThreadFactory("auth-token-%d", true));

    @Autowired
    private SlardarAuthenticateService authenticateService;

    /**
     * 忽略的url pattern
     */
    private final String[] ignoredUrls;

    private final List<AntPathRequestMatcher> ignoredPathRequestMatchers = new ArrayList<>(1);


    public String[] getIgnoredUrls() {
        return ignoredUrls;
    }

    public List<AntPathRequestMatcher> getIgnoredPathRequestMatchers() {
        return ignoredPathRequestMatchers;
    }

    public SlardarTokenRequiredFilter(String[] ignoredUrls) {
        this.ignoredUrls = ignoredUrls;
        if (ignoredUrls != null && ignoredUrls.length > 0) {
            Arrays.stream(ignoredUrls).forEach(url -> ignoredPathRequestMatchers.add(new AntPathRequestMatcher(url)));
        }
        // 忽略 /logout
        ignoredPathRequestMatchers.add(new AntPathRequestMatcher(AUTH_LOGOUT_URL));
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
        final String authToken = authenticateService.getTokenValueFromServlet(request);
        SlardarException tokenValidateEx = null;
        if (StringUtils.hasText(authToken)) {
            LoginDeviceType deviceType = null;
            String username = null;
            try {
                deviceType = getDeviceType(request);
                username = authenticateService.getUsernameFromTokenValue(authToken);
            } catch (Exception e) {
                tokenValidateEx = new SlardarException(e.getLocalizedMessage());
            }
            if (authenticateService.isExpired(authToken, deviceType)) {
                tokenValidateEx = new SlardarException(TOKEN_EXPIRED);
            }
            if (Objects.isNull(tokenValidateEx) && Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {
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
                    LoginDeviceType finalDeviceType = deviceType;
                    POOL.submit(() -> {
                        try {
                            authenticateService.renewToken(authToken, finalDeviceType);
                        } catch (Exception e) {
                            log.error("Failed to renew token asynchronously", e);
                        }
                    });
                } else {
                    // 账户过期
                    tokenValidateEx = new SlardarException("account has been expired or forbidden");
                }
            }
        } else {
            tokenValidateEx = new SlardarException(TOKEN_REQUIRED);
        }
        if (tokenValidateEx != null) {
            log.error("Failed to parse token or user disabled. token:  {}, error: {}", authToken, tokenValidateEx.getLocalizedMessage());
            sendError(request, response, HttpStatus.UNAUTHORIZED, tokenValidateEx);
        } else {
            try {
                filterChain.doFilter(request, response);
            } finally {
                // 避免 threadLocal 内存溢出
                SlardarSecurityHelper.clear();
                SecurityContextHolder.clearContext();
            }
        }
    }

    @PreDestroy
    public void shutdownPool() {
        POOL.shutdown();
    }
}