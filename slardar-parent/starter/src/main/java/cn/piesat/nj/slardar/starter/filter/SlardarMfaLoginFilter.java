package cn.piesat.nj.slardar.starter.filter;

import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.SlardarUserDetails;
import cn.piesat.nj.slardar.starter.authenticate.SlardarAuthentication;
import cn.piesat.nj.slardar.starter.authenticate.mfa.SlardarMfaAuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static cn.piesat.nj.slardar.starter.support.HttpServletUtil.*;
import static cn.piesat.nj.slardar.starter.support.SecUtil.GSON;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/29
 */
public class SlardarMfaLoginFilter extends AbstractAuthenticationProcessingFilter {


    private final SlardarMfaAuthService mfaAuthService;

    public SlardarMfaLoginFilter(SlardarMfaAuthService mfaAuthService,
                                 AuthenticationFailureHandler authenticationFailureHandler,
                                 AuthenticationSuccessHandler authenticationSuccessHandler) {
        super(new AntPathRequestMatcher("/mfa-login"));
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
            // 失败如何处理
            if (!b) {
//                sendJson(response, makeErrorResult("MFA 认证失败，口令可能已过期或不存在", HttpStatus.UNAUTHORIZED.value()), HttpStatus.UNAUTHORIZED, request.getHeader("Origin"));
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
