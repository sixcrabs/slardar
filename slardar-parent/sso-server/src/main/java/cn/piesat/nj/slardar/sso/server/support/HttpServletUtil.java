package cn.piesat.nj.slardar.sso.server.support;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static cn.piesat.nj.slardar.sso.server.support.SsoConstants.CODE_20001;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
public final class HttpServletUtil {

    /**
     * 转发请求
     */
    public static Object forward(final HttpServletRequest request, final HttpServletResponse response, String path) throws SsoException {
        try {
            request.getRequestDispatcher(path).forward(request, response);
            return null;
        } catch (ServletException | IOException e) {
            throw new SsoException(e).setCode(CODE_20001);
        }
    }

    /**
     * 返回当前请求path (不包括上下文名称)
     */
    public static String getRequestPath(final HttpServletRequest request) {
        return request.getServletPath();
    }

    /**
     * 返回当前请求的url，例：`http://xxx.com/test`
     */
    public static String getUrl(final HttpServletRequest request) {
        return request.getRequestURL().toString();
    }

    /**
     * 返回当前请求的类型
     */
    public static String getMethod(final HttpServletRequest request) {
        return request.getMethod();
    }

    /**
     * get param value
     * @param request
     * @param name
     * @return
     */
    public static String getParam(final HttpServletRequest request, String name) {
        return request.getParameter(name);
    }

    /**
     * get value from `Cookie`
     */
    public static String getCookieValue(final HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie != null && name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * get all param as map
     * @param request
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getParamsAsMap(final HttpServletRequest request) {
        Map<String, String> res = new HashMap(1);
        Enumeration<String> names = request.getParameterNames();
        if (null != names) {
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                String value = request.getParameter(name);
                res.put(name, value);
            }
        }
        return res;
    }

    /**
     * get all headers as map
     * @param request
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getHeadersAsMap(final HttpServletRequest request) {
        Map<String, String> res = new HashMap(1);
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            res.put(name, request.getHeader(name));
        }
        return res;
    }
}
