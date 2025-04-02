package cn.piesat.v.slardar.starter.filter.request;

import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.core.annotation.SlardarIgnore;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.starter.SlardarAuthenticateService;
import cn.piesat.v.slardar.starter.SlardarUserDetails;
import cn.piesat.v.slardar.starter.authenticate.SlardarAuthentication;
import cn.piesat.v.slardar.starter.support.Base64;
import cn.piesat.v.misc.hutool.mini.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static cn.piesat.v.slardar.starter.support.HttpServletUtil.*;

/**
 * <p>
 * filter for `BasicAuth`
 * 只拦截配置的请求（少数优先）
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/4/15
 */
public class SlardarBasicAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SlardarBasicAuthFilter.class);

    @Autowired
    private UserDetailsService userDetailsService;

    @Resource
    private SlardarAuthenticateService tokenService;

    private final SlardarSpiContext spiContext;

    public SlardarBasicAuthFilter(String[] filterUrls, SlardarSpiContext spiContext) {
        this.spiContext = spiContext;
        if (filterUrls != null && filterUrls.length > 0) {
            Arrays.stream(filterUrls).forEach(url -> requiredPathRequestMatchers.add(new AntPathRequestMatcher(url)));
        }
    }

    /**
     * 需要匹配 BasicAuth 的请求路径
     */
    private final List<AntPathRequestMatcher> requiredPathRequestMatchers = new ArrayList<>(1);


    /**
     * 哪些不需要经过 此过滤器
     *
     * @param request current HTTP request
     * @return whether the given request should <i>not</i> be filtered
     * @throws ServletException in case of errors
     */
    @Override
    protected boolean shouldNotFilter(@Nonnull HttpServletRequest request) throws ServletException {
        return requiredPathRequestMatchers.stream().noneMatch(matcher -> matcher.matcher(request).isMatch());
    }

    /**
     * 外部添加过滤的 url pattern
     *
     * @param antPattern
     * @param method
     * @see SlardarIgnore
     */
    public void addUrlPattern(String antPattern, String method) {
        requiredPathRequestMatchers.add(new AntPathRequestMatcher(antPattern, StringUtil.isBlank(method) ? null : method));
    }

    /**
     * Same contract as for {@code doFilter}, but guaranteed to be
     * just invoked once per request within a single request thread.
     * See {@link #shouldNotFilterAsyncDispatch()} for details.
     * <p>Provides HttpServletRequest and HttpServletResponse arguments instead of the
     * default ServletRequest and ServletResponse ones.
     *
     * @param request
     * @param response
     * @param filterChain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authToken = tokenService.getTokenValue(request);
        PasswordEncoder passwordEncoder = spiContext.getBeanOrDefault(PasswordEncoder.class, new BCryptPasswordEncoder());
        SlardarException tokenValidateEx = null;
        if (StringUtils.hasText(authToken) && authToken.trim().startsWith("Basic ")) {
            String encoded = authToken.substring(6);
            String username = null;
            String pwd = null;
            try {
                // user:pwd
                String decodedToken = new String(Base64.decodeBase64(encoded.getBytes()), StandardCharsets.UTF_8);
                String[] userInfoParts = decodedToken.split(":");
                if (userInfoParts.length != 2) {
                    logger.error("Invalid token format");
                    throw new SlardarException("Invalid Basic-Auth token format.");
                }
                username = userInfoParts[0];
                pwd = userInfoParts[1];
            } catch (Exception e) {
                tokenValidateEx = new SlardarException(e.getLocalizedMessage());
            }
            if (Objects.isNull(tokenValidateEx) && StringUtil.isNotBlank(username) && StringUtil.isNotBlank(pwd)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 加载详细信息
                SlardarUserDetails userDetails = (SlardarUserDetails) userDetailsService.loadUserByUsername(username);
                // 验证密码是否匹配(加密后验证)
                if (!passwordEncoder.matches(pwd, userDetails.getPassword())) {
                    logger.error("Incorrect password");
                    tokenValidateEx = new SlardarException("Incorrect password ");
                } else if (!userDetails.isEnabled()) {
                    // 账户过期
                    tokenValidateEx = new SlardarException("Your account has expired");
                } else {
                    // TESTME: 用户信息的存储在 basicAuth 模式下是否需要改进?
                    SlardarAuthentication authenticationToken = new SlardarAuthentication(username, "basic", userDetails);
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    authenticationToken.setAuthenticated(true);
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } else {
                tokenValidateEx = new SlardarException("Token is invalid");
            }
        } else {
            tokenValidateEx = new SlardarException("Authorization token is required.");
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
    }
}
