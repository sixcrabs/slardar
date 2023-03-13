package cn.piesat.nj.slardar.starter.filter;

import cn.piesat.nj.slardar.starter.config.SlardarProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 处理 /userdetails 请求
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/13
 */
public class SlardarUserDetailsProcessingFilter extends GenericFilterBean {

    private final RequestMatcher requestMatcher;

    private static ObjectMapper globalObjectMapper = new ObjectMapper();

    public SlardarUserDetailsProcessingFilter(SlardarProperties properties) {
        this.requestMatcher = new AntPathRequestMatcher("/userdetails", "POST");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;

        // 详细用户对象
        Map<String, Object> details = new HashMap<>(1);

        // TODO 获取 token 验证、拿到 userdetails
        response.setStatus(HttpStatus.OK.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        globalObjectMapper.writeValue(response.getWriter(), details);
        response.getWriter().flush();

        chain.doFilter(request, response);
    }
}
