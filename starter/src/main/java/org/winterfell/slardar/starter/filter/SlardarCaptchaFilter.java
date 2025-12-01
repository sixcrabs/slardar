package org.winterfell.slardar.starter.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.winterfell.slardar.starter.SlardarProperties;
import org.winterfell.slardar.starter.support.CaptchaComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;
import org.winterfell.slardar.starter.support.SimpleRateLimiter;

import java.io.IOException;
import java.io.OutputStream;

import static org.winterfell.slardar.starter.support.HttpServletUtil.getSessionId;

/**
 * <p>
 * 返回验证码图片
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/16
 */
public class SlardarCaptchaFilter extends GenericFilterBean {

    private final RequestMatcher requestMatcher;

    @Autowired
    private CaptchaComponent captchaComponent;

    // 每秒10次
    private static final SimpleRateLimiter RATE_LIMITER = new SimpleRateLimiter(10);

    public SlardarCaptchaFilter(SlardarProperties properties) {
        this.requestMatcher = new AntPathRequestMatcher(properties.getCaptcha().getUrl(), HttpMethod.GET.name());
    }


    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (!this.requestMatcher.matches(request)) {
            chain.doFilter(request, response);
        } else {
            if (!RATE_LIMITER.tryAcquire()) {
                response.sendError(429, "Too many requests");
                return;
            }
            CaptchaComponent.CaptchaPayload payload = captchaComponent.generate(getSessionId(request));
            try (OutputStream os = response.getOutputStream()) {
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Cache-Control", "no-store, max-age=0");
                response.setHeader("X-Content-Type-Options", "nosniff");
                response.setHeader("Content-Security-Policy", "default-src 'none'");
                response.setHeader("X-Captcha-ID", payload.getSessionId());

                response.setContentType(MediaType.IMAGE_PNG_VALUE);
                response.setStatus(HttpServletResponse.SC_OK);
                os.write(payload.getImgBytes());
                os.flush();
            } catch (IOException e) {
                response.sendError(500, "Failed to generate captcha");
                // 同时记录完整日志
                logger.error("Captcha generation failed", e);
            }
        }
    }
}