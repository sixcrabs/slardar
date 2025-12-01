package org.winterfell.slardar.starter.filter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.winterfell.slardar.core.SlardarException;
import org.winterfell.slardar.starter.authenticate.SlardarUserDetails;
import org.winterfell.slardar.starter.authenticate.SlardarAuthentication;
import org.winterfell.slardar.starter.authenticate.mfa.SlardarMfaAuthService;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static org.winterfell.slardar.starter.support.HttpServletUtil.*;
import static org.winterfell.slardar.starter.support.SecUtil.GSON;

/**
 * <p>
 * MFA 认证过滤器
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/29
 */
public class SlardarMfaFilter extends AbstractAuthenticationProcessingFilter {


    private final SlardarMfaAuthService mfaAuthService;

    public SlardarMfaFilter(SlardarMfaAuthService mfaAuthService,
                            AuthenticationFailureHandler authenticationFailureHandler,
                            AuthenticationSuccessHandler authenticationSuccessHandler) {
        super(PathPatternRequestMatcher.withDefaults().matcher("/mfa-login"));
        this.mfaAuthService = mfaAuthService;
        setAuthenticationManager(authentication -> null);
        setAuthenticationSuccessHandler(authenticationSuccessHandler);
        setAuthenticationFailureHandler(authenticationFailureHandler);
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        Map<String, String> requestParam = getRequestParam(request);
        if (requestParam.isEmpty()) {
            requestParam = GSON.fromJson(getRequestPostStr(request), Map.class);
        }
        if (Objects.isNull(requestParam)) {
            throw new AuthenticationServiceException("MFA-Login params cannot be null");
        }
        // 验证 otp-code 和 key
        String key = requestParam.get("key");
        String code = requestParam.get("code");
        try {
            boolean b = mfaAuthService.verify(key, code);
            if (!b) {
                throw new AuthenticationServiceException("MFA 认证失败，口令可能已过期或不存在");
            }
            // 设置已认证
            SlardarUserDetails userDetails = mfaAuthService.getUserDetails(key);
            SlardarAuthentication authentication = new SlardarAuthentication(userDetails);
            authentication.setAuthenticated(true);
            return authentication;

        } catch (SlardarException e) {
            throw new AuthenticationServiceException(e.getLocalizedMessage());
        }
    }
}