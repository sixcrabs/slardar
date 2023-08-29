package cn.piesat.nj.slardar.starter.filter;

import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.authenticate.mfa.SlardarMfaAuthService;
import org.springframework.security.authentication.AuthenticationServiceException;
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
public class SlardarMfaLoginFilter extends GenericFilterBean {

    private final RequestMatcher requestMatcher;

    private final SlardarMfaAuthService mfaAuthService;

    public SlardarMfaLoginFilter(SlardarMfaAuthService mfaAuthService) {
        this.requestMatcher = new AntPathRequestMatcher("/mfa-login");
        this.mfaAuthService = mfaAuthService;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (!this.requestMatcher.matches(request)) {
            chain.doFilter(request, response);
        } else {
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
                // TODO 失败后如何处理？

            } catch (SlardarException e) {
                e.printStackTrace();
            }
        }

    }
}
