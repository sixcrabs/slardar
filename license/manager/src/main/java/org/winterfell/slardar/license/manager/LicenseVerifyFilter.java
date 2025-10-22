package org.winterfell.slardar.license.manager;

import org.winterfell.slardar.starter.config.customizer.SlardarHttpSecurityCustomizer;
import org.winterfell.slardar.starter.filter.SlardarLoginFilter;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static org.winterfell.slardar.license.manager.LicenseManageRequestFilter.LIC_REQ_URL;
import static org.winterfell.slardar.license.manager.LicenseManageRequestFilter.LIC_VIEW_URL;
import static org.winterfell.slardar.license.manager.support.LicenseManagerUtil.sendJson;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/9/26
 */
public class LicenseVerifyFilter extends OncePerRequestFilter implements SlardarHttpSecurityCustomizer {

    private final List<AntPathRequestMatcher> ignoredPathRequestMatchers = Lists.newArrayList(new AntPathRequestMatcher(LIC_VIEW_URL),
            new AntPathRequestMatcher(LIC_REQ_URL),
            new AntPathRequestMatcher("/js/**"), new AntPathRequestMatcher("/css/**"));

    private final LicenseManageRequestHandler licenseManageRequestHandler;

    public LicenseVerifyFilter(LicenseManageRequestHandler licenseManageRequestHandler) {
        this.licenseManageRequestHandler = licenseManageRequestHandler;
    }


    @Override
    protected boolean shouldNotFilter(@NotNull HttpServletRequest request) throws ServletException {
        return ignoredPathRequestMatchers.stream().anyMatch(matcher -> matcher.matcher(request).isMatch());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            boolean verified = licenseManageRequestHandler.verifyLicense();
            if (verified) {
                filterChain.doFilter(request, response);
            }
        } catch (LicenseException e) {
            sendJson(response, HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * 将这个过滤器加到httpSecurity中
     *
     * @param httpSecurity @link HttpSecurity
     */
    @Override
    public void customize(HttpSecurity httpSecurity) {
        httpSecurity.addFilterBefore(this, SlardarLoginFilter.class);
    }
}
