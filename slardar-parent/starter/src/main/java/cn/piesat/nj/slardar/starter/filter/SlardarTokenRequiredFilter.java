package cn.piesat.nj.slardar.starter.filter;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.SlardarTokenService;
import cn.piesat.nj.slardar.starter.SlardarUserDetails;
import cn.piesat.nj.slardar.starter.support.LoginDeviceType;
import cn.piesat.nj.slardar.starter.support.SecUtil;
import cn.piesat.nj.slardar.starter.support.SlardarAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static cn.piesat.nj.slardar.starter.support.HttpServletUtil.getDeviceType;

/**
 * 处理 token 验证
 * 拦截所有请求
 *
 * @author JiajieZhang
 * @date 2022/9/23
 * @description token过滤器
 */
@Slf4j
public class SlardarTokenRequiredFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService;


    private static final ExecutorService POOL = new ThreadPoolExecutor(4, Runtime.getRuntime().availableProcessors() * 2,
            3000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(512), new NamedThreadFactory("auth-token-%d", true));

    @Autowired
    private SlardarTokenService tokenService;

    /**
     * 忽略的url pattern
     */
    private final String[] ignoredUrls;


    public String[] getIgnoredUrls() {
        return ignoredUrls;
    }

    public SlardarTokenRequiredFilter(String[] ignoredUrls) {
        this.ignoredUrls = ignoredUrls;
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
        return Arrays.stream(ignoredUrls).anyMatch(url -> new AntPathRequestMatcher(url).matcher(request).isMatch());
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
        SlardarException loginException = null;
        if (StringUtils.hasText(authToken)) {
            LoginDeviceType deviceType = getDeviceType(request);
            String username = tokenService.getUsername(authToken);
            if (!tokenService.isExpired(authToken, deviceType)) {
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 加载详细信息
                    SlardarUserDetails userDetails = (SlardarUserDetails) userDetailsService.loadUserByUsername(username);
                    // 判断当前登陆人的账户是否可用
                    if (userDetails.isEnabled()) {
                        SlardarAuthenticationToken authenticationToken = new SlardarAuthenticationToken(username, "", userDetails);
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        String finalAuthToken = authToken;
                        POOL.submit(() -> {
                            // token 续期
                            tokenService.renewToken(finalAuthToken, deviceType);
                        });

                    } else {
                        // 账户过期
                        loginException = new SlardarException("account has expired");
                    }
                }
            }
        }
        if (loginException != null) {
            //将异常分发到/remoteLoginException
            forwardRequest(request, response, loginException, "remoteLoginException", "/remoteLoginException");
        }
        filterChain.doFilter(request, response);
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
        request.getRequestDispatcher(url).forward(request, response);
    }
}