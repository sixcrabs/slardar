package io.github.sixcrabs.slardar.ext.apisix;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;
import org.winterfell.misc.hutool.mini.StringUtil;
import io.github.sixcrabs.slardar.core.SlardarException;
import io.github.sixcrabs.slardar.starter.authenticate.SlardarAuthenticateService;
import io.github.sixcrabs.slardar.starter.authenticate.SlardarUserDetails;
import io.github.sixcrabs.slardar.starter.config.customizer.SlardarIgnoringCustomizer;
import io.github.sixcrabs.slardar.starter.support.LoginDeviceType;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static io.github.sixcrabs.slardar.core.Constants.TOKEN_EXPIRED;
import static io.github.sixcrabs.slardar.core.Constants.TOKEN_REQUIRED;
import static io.github.sixcrabs.slardar.starter.support.HttpServletUtil.getDeviceType;

/**
 * <p>
 * 提供 `/verify` 接口，用于 apisix 的 forward-auth 验证 token
 * </p>
 *
 * @author Alex
 * @since 2025/12/16
 */
@Slf4j
public class ApisixVerifyFilter extends GenericFilterBean implements SlardarIgnoringCustomizer {

    @Resource
    private SlardarAuthenticateService authenticateService;

    @Resource
    private UserDetailsService userDetailsService;

    private final RequestMatcher requestMatcher;

    private final SlardarApisixProperties properties;

    public ApisixVerifyFilter(SlardarApisixProperties properties) {
        this.properties = properties;
        this.requestMatcher = new AntPathRequestMatcher(properties.getVerifyUrl());
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (!this.requestMatcher.matches(request)) {
            chain.doFilter(request, response);
        } else {
            final String accessToken = authenticateService.getTokenValueFromServlet(request);
            SlardarException tokenValidateEx = StringUtil.isEmpty(accessToken) ? new SlardarException(TOKEN_REQUIRED) : null;
            if (Objects.isNull(tokenValidateEx)) {
                LoginDeviceType deviceType = getDeviceType(request);
                if (authenticateService.isExpired(accessToken, deviceType)) {
                    tokenValidateEx = new SlardarException(TOKEN_EXPIRED);
                    sendEx(response, HttpStatus.UNAUTHORIZED, tokenValidateEx);
                    return;
                }
                String username = authenticateService.getUsernameFromTokenValue(accessToken);
                // 加载详细信息
                SlardarUserDetails userDetails = (SlardarUserDetails) userDetailsService.loadUserByUsername(username);
                if (userDetails.isEnabled()) {
                    sendSuccess(response, "success", userDetails);
                } else {
                    // 账户过期
                    sendEx(response, HttpStatus.UNAUTHORIZED, new SlardarException("account has been expired or forbidden"));
                }
            } else {
                sendEx(response, HttpStatus.UNAUTHORIZED, tokenValidateEx);
            }
        }
    }

    private void sendEx(HttpServletResponse response, HttpStatus httpStatus, Exception exception) {
        response.setStatus(httpStatus.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.setHeader("X-error", exception.getLocalizedMessage());
        response.setHeader("Access-Control-Allow-Credentials", "true");
//        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
        try {
            response.getWriter().print(exception.getLocalizedMessage());
            response.getWriter().flush();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    private void sendSuccess(HttpServletResponse response, String message, SlardarUserDetails userDetails) {
        response.setStatus(HttpStatus.OK.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
        response.setHeader("X-User-ID", userDetails.getAccount().getId());
        response.setHeader("X-User-Name", userDetails.getUsername());
        try {
            response.getWriter().print(message);
            response.getWriter().flush();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    /**
     * 自定义过滤需要忽略的url
     *
     * @param antPatterns
     */
    @Override
    public void customize(List<String> antPatterns) {
        antPatterns.add(properties.getVerifyUrl());
    }
}