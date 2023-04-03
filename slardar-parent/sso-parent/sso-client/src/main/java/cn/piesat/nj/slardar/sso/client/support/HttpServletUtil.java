package cn.piesat.nj.slardar.sso.client.support;

import cn.hutool.core.map.MapUtil;
import cn.piesat.nj.slardar.core.SlardarException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static cn.piesat.nj.slardar.core.Constants.BEARER;


/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
public final class HttpServletUtil {

    public static final Logger log = LoggerFactory.getLogger(HttpServletUtil.class);

    public static final Gson GSON = new GsonBuilder().create();

    /**
     * 转发请求
     */
    public static Object forward(final HttpServletRequest request, final HttpServletResponse response, String path) throws SlardarException {
        try {
            request.getRequestDispatcher(path).forward(request, response);
            return null;
        } catch (ServletException | IOException e) {
            throw new SlardarException(e); //.setCode(CODE_20001);
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
     *
     * @param request
     * @param name
     * @return
     */
    public static String getParam(final HttpServletRequest request, String name) {
        return request.getParameter(name);
    }


    public static String getTokenValue(HttpServletRequest request) {
        String tokenValue = null;
        // TBD:
        String tokenKey = "Authorization";
        // 1. 尝试从request attributes里读取
        Object attribute = request.getAttribute(tokenKey);
        if (attribute != null) {
            tokenValue = String.valueOf(attribute);
        }
        if (Objects.isNull(tokenValue)) {
            // 2. 尝试从请求体里面读取
            tokenValue = request.getParameter(tokenKey);
        }
        if (Objects.isNull(tokenValue)) {
            // 3. 尝试从header里读取
            tokenValue = request.getHeader(tokenKey);
        }
        if (Objects.isNull(tokenValue)) {
            // 4. 尝试从cookie里读取
            tokenValue = getCookieValue(request, tokenKey);
        }
        if (tokenValue != null && tokenValue.startsWith(BEARER)) {
            tokenValue = tokenValue.replace(BEARER, "");
        }
        return tokenValue;
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
     * Set-Cookie: name=value; Max-Age=100000; Expires=Tue, 05-Oct-2021 20:28:17 GMT; Domain=localhost; Path=/; Secure; HttpOnly; SameSite=Lax
     *
     * @param response
     * @param cookieName
     * @param cookieValue
     * @param maxAge
     * @param domain
     * @param path
     * @param sameSite  第三方限制级别（Strict=完全禁止，Lax=部分允许，None=不限制）
     */
    public static void setCookie(final HttpServletResponse response, String cookieName, String cookieValue,
                                 Integer maxAge, String domain, String path, String sameSite) {
        StringBuilder sb = new StringBuilder();
        sb.append(cookieName).append("=").append(cookieValue);
        if (maxAge >= 0) {
            sb.append("; Max-Age=").append(maxAge);
            String expires;
            if (maxAge == 0) {
                expires = Instant.EPOCH.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME);
            } else {
                expires = OffsetDateTime.now().plusSeconds(maxAge).format(DateTimeFormatter.RFC_1123_DATE_TIME);
            }
            sb.append("; Expires=").append(expires);
        }
        if (StringUtils.hasText(domain)) {
            sb.append("; Domain=").append(domain);
        }
        if (StringUtils.hasText(path)) {
            sb.append("; Path=").append(path);
        }
        boolean secure = true;
        if(secure) {
            sb.append("; Secure");
        }
//        if(httpOnly) {
//            sb.append("; HttpOnly");
//        }
        if(StringUtils.hasText(sameSite)) {
            sb.append("; SameSite=").append(sameSite);
        }
        response.addHeader("Set-Cookie", sb.toString());
    }

    /**
     * get all param as map
     *
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
     *
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

    /**
     * get session id from request
     *
     * @param request
     * @return
     */
    public static String getSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession();
        log.debug("session:{}", session == null ? "null" : session.toString());
        if (session != null) {
            String sessionId = session.getId();
            log.debug("sessionId:{}", sessionId);
            return sessionId;
        }
        return "";
    }

    public static String getRequestPostStr(HttpServletRequest request) throws IOException {
        byte[] buffer = getRequestPostBytes(request);
        String charEncoding = request.getCharacterEncoding();
        if (charEncoding == null) {
            charEncoding = "UTF-8";
        }
        assert buffer != null;
        return new String(buffer, charEncoding);
    }

    private static byte[] getRequestPostBytes(HttpServletRequest request) throws IOException {
        int contentLength = request.getContentLength();
        if (contentLength < 0) {
            return null;
        }
        byte[] buffer = new byte[contentLength];
        for (int i = 0; i < contentLength; ) {
            int readlen = request.getInputStream().read(buffer, i,
                    contentLength - i);
            if (readlen == -1) {
                break;
            }
            i += readlen;
        }
        return buffer;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getRequestParam(final HttpServletRequest request) {
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

    @SuppressWarnings("unchecked")
    public static Map<String, String> getHeaders(final HttpServletRequest request) {
        Map<String, String> res = new HashMap(1);
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            res.put(name, request.getHeader(name));
        }
        return res;
    }

    /**
     * send json to response
     *
     * @param response
     * @param result
     * @throws IOException
     */
    public static void sendJson(HttpServletResponse response, Serializable result, HttpStatus httpStatus) {
        response.setStatus(httpStatus.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try {
            response.getWriter().write((result instanceof String) ? result.toString() : GSON.toJson(result));
            response.getWriter().flush();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public static void sendJsonOK(HttpServletResponse response, Serializable result) {
        sendJson(response, result, HttpStatus.OK);
    }

    public static HashMap<String, Object> makeResult(Object result, int code) {
        HashMap<String, Object> ret = MapUtil.of("code", code);
        ret.put("data", result);
        return ret;
    }

    public static HashMap<String, Object> makeErrorResult(String msg, int code) {
        HashMap<String, Object> ret = MapUtil.of("code", code);
        ret.put("data", new Object());
        ret.put("message", msg);
        return ret;
    }

    public static HashMap<String, Object> makeSuccessResult(Object result) {
        HashMap<String, Object> ret = MapUtil.of("code", HttpStatus.OK.value());
        ret.put("data", result);
        ret.put("message", "ok");
        return ret;
    }
}
