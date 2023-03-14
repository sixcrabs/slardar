package cn.piesat.nj.slardar.helper;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;


/**
 * <p>
 * 识别从网关（或其他）来的请求中的token
 * userId isAdmin 信息
 * 并save 到 对应holder 中
 * </p>
 *
 * @author alex
 * @version v1.0, 2019/3/14
 */
@Component
public class GatewayUserInfoFilter extends OncePerRequestFilter implements Ordered {

    public GatewayUserInfoFilter() {
    }

    private static final String AUTHZ_HEADER_ATTR_PREFIX = "x-auth-";

    /**
     * TODO
     * 请求过滤器
     *
     * @param request
     * @param response
     * @param chain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        SecurityHelper.SecurityContext context = new SecurityHelper.SecurityContext(
                Boolean.parseBoolean(request.getHeader(AUTHZ_HEADER_ATTR_PREFIX + "isSysAdmin")),
                request.getHeader(AUTHZ_HEADER_ATTR_PREFIX + "userId"),
                request.getHeader(AUTHZ_HEADER_ATTR_PREFIX + "userLoginName"))
                .setUserRealName(URLDecoder.decode(request.getHeader(AUTHZ_HEADER_ATTR_PREFIX + "userRealName") == null ? "" : request.getHeader("userRealName"), "utf-8"));
//        Enumeration<String> headerNames = request.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String name = headerNames.nextElement();
//            if (name.startsWith(USER_ATTR_PREFIX)) {
//                context.addAttribute(name.replace(USER_ATTR_PREFIX, ""), URLDecoder.decode(request.getHeader(name), "utf-8"));
//            }
//        }
        SecurityHelper.setContext(context);
        chain.doFilter(request, response);
    }


    @Override
    public int getOrder() {
        return -2023;
    }
}
