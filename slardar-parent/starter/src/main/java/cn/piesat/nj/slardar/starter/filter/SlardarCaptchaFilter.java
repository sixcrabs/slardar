package cn.piesat.nj.slardar.starter.filter;

import cn.piesat.nj.slardar.starter.support.SecUtil;
import cn.piesat.nj.slardar.starter.support.captcha.CaptchaComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
import java.io.OutputStream;

import static cn.piesat.nj.slardar.starter.support.HttpServletUtil.getSessionId;

/**
 * <p>
 * 返回验证码
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/16
 */
public class SlardarCaptchaFilter extends GenericFilterBean {

    private final RequestMatcher requestMatcher;

    @Autowired
    private CaptchaComponent captchaComponent;

    public SlardarCaptchaFilter() {
        this.requestMatcher = new AntPathRequestMatcher("/captcha", HttpMethod.GET.name());
    }


    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (!this.requestMatcher.matches(request)) {
            chain.doFilter(request, response);
        } else {
            // 生成验证码
            CaptchaComponent.CaptchaPayload payload = captchaComponent.generate(getSessionId(request));
            try (OutputStream os = response.getOutputStream()) {
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Cache-Control", "no-cache");
                response.setDateHeader("Expires", 0);
                response.setContentType(MediaType.IMAGE_PNG_VALUE);
                os.write(payload.getImgBytes());
                os.flush();
            } catch (IOException e) {
                try {
                    response.sendError(500, "server error");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
