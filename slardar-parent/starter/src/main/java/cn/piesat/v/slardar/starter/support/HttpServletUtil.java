package cn.piesat.v.slardar.starter.support;

import cn.piesat.v.misc.hutool.mini.StringUtil;
import cn.piesat.v.misc.hutool.mini.thread.ThreadUtil;
import cn.piesat.v.slardar.core.SlardarException;
import com.google.gson.JsonSyntaxException;
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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static cn.piesat.v.slardar.core.Constants.MOBILE_AGENTS;
import static cn.piesat.v.slardar.starter.support.SecUtil.GSON;
import static cn.piesat.v.slardar.starter.support.SecUtil.objectMapper;


/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
public final class HttpServletUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpServletUtil.class);

    /**
     * 转发请求
     */
    public static Object forward(final HttpServletRequest request, final HttpServletResponse response, String path) throws SlardarException {
        try {
            request.getRequestDispatcher(path).forward(request, response);
            return null;
        } catch (ServletException | IOException e) {
            ///.setCode(CODE_20001);
            throw new SlardarException(e);
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
     * @param sameSite    第三方限制级别（Strict=完全禁止，Lax=部分允许，None=不限制）
     * @param httpOnly
     */
    public static void setCookie(final HttpServletResponse response, String cookieName, String cookieValue,
                                 Integer maxAge, String domain, String path, String sameSite, boolean httpOnly) {
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
        sb.append("; Secure");
        if (StringUtils.hasText(sameSite)) {
            sb.append("; SameSite=").append(sameSite);
        }
        // 加上 httpOnly
        if (httpOnly) {
            sb.append("; HttpOnly");
        }
        response.addHeader("Set-Cookie", sb.toString());
    }

    /**
     * get device type of request
     *
     * @param request
     * @return
     */
    public static LoginDeviceType getDeviceType(HttpServletRequest request) {
        return isFromMobile(request) ? LoginDeviceType.APP : LoginDeviceType.PC;
    }

    /**
     * request is from mobile
     *
     * @param request
     * @return
     */
    public static boolean isFromMobile(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        boolean flag = false;
        if (!ua.contains("Windows NT") || (ua.contains("Windows NT")
                && ua.contains("compatible; MSIE 9.0;"))) {
            if (!ua.contains("Windows NT") && !ua.contains("Macintosh")) {
                for (String item : MOBILE_AGENTS) {
                    if (ua.contains(item)) {
                        flag = true;
                        break;
                    }
                }
            }
        }
        return flag;
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
     * // FIXME: UT000010: Session is invalid
     *
     * @param request
     * @return
     */
    public static String getSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
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
        if (Objects.isNull(buffer)) {
            return StringUtil.EMPTY;
        }
        String charEncoding = request.getCharacterEncoding();
        if (charEncoding == null) {
            charEncoding = "UTF-8";
        }
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

    public static Map<String, String> getRequestParamWithPostStr(final HttpServletRequest request) throws IOException {
        Map<String, String> map = getRequestParam(request);
        if (!map.isEmpty()) {
            return map;
        }
        String body = getRequestPostStr(request);
        if (StringUtil.isBlank(body)) {
            return Collections.emptyMap();
        }
        try {
            return GSON.fromJson(body, Map.class);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Invalid JSON format in login request");
        }

    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getHeaders(final HttpServletRequest request) {
        Map<String, String> res = new HashMap<>(1);
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
    public static void sendJson(HttpServletResponse response, Serializable result, HttpStatus httpStatus, String originValue) {
        response.setStatus(httpStatus.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Origin", originValue);
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
        try {
            objectMapper.writeValue(response.getWriter(), result);
            // FIXME: 序列化 时间格式问题
//            response.getWriter().write((result instanceof String) ? result.toString() : GSON.toJson(result));
            response.getWriter().flush();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public static void sendJsonOk(HttpServletResponse response, Serializable result) {
        sendJson(response, result, HttpStatus.OK, "");
    }

    public static HashMap<String, Object> makeResult(Object result, int code, String msg) {
        HashMap<String, Object> ret = new HashMap<>(2);
        ret.put("code", code);
        ret.put("data", result);
        ret.put("message", msg);
        return ret;
    }


    public static HashMap<String, Object> makeSuccessResult(Object result) {
        HashMap<String, Object> ret = new HashMap<>(3);
        ret.put("code", HttpStatus.OK.value());
        ret.put("data", result);
        ret.put("message", "ok");
        return ret;
    }

    public static HashMap<String, Object> makeErrorResult(String msg, int code) {
        HashMap<String, Object> ret = new HashMap<>(3);
        ret.put("code", code);
        ret.put("data", null);
        ret.put("message", msg);
        return ret;
    }

    //本地ip地址
    public static final String LOCAL_IP = "127.0.0.1";
    //默认ip地址
    public static final String DEFAULT_IP = "0:0:0:0:0:0:0:1";
    //默认ip地址长度
    public static final int DEFAULT_IP_LENGTH = 15;

    public static InetAddress localAddress;

    static {
        getLocalIpAsync();
    }

    private static void getLocalIpAsync() {
        ThreadUtil.newThread(() -> {
            try {
                // TBD: 这行代码执行有时候会特别慢 预先处理
                localAddress = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                log.error("InetAddress getLocalHost error In HttpUtils getRealIpAddress: ", e);
            }

        }, "local-ip-get").start();
    }

    /**
     * 获取ip地址
     *
     * @param request
     * @return
     */
    public static String getRequestIpAddress(HttpServletRequest request) {
        String ipAddress = null;
        try {
            ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                //apache服务代理
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                //weblogic 代理
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("HTTP_CLIENT_IP");
            }

            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                //nginx代理
                ipAddress = request.getHeader("X-Real-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if (StringUtil.equals(ipAddress, LOCAL_IP) || StringUtil.equals(ipAddress, DEFAULT_IP)) {
                    //根据网卡取本机配置的IP
                    if (Objects.isNull(localAddress)) {
                        getLocalIpAsync();
                    }
                    InetAddress iNet = localAddress;
                    ipAddress = iNet.getHostAddress();
                }
            }

            //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            //"***.***.***.***".length() = 15
            if (StringUtil.isNotBlank(ipAddress) && ipAddress.length() > DEFAULT_IP_LENGTH) {
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }

        } catch (Exception e) {
            log.error("HttpServletUtil ERROR ", e);
        }
        return ipAddress;
    }
}
