package cn.piesat.nj.slardar.starter.support;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static cn.piesat.nj.slardar.core.Constants.ANONYMOUS;
import static cn.piesat.nj.slardar.core.Constants.MOBILE_AGENTS;

/**
 * <p>
 * .
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/27
 */
@Slf4j
public final class SecUtil {

    private SecUtil() {
    }

    public static final Gson GSON = new GsonBuilder().create();


    /**
     * request is from mobile
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
     * 获取用户登录名
     * @return
     */
    public static String getCurrentUsername() {
        SlardarAuthenticationToken authentication = (SlardarAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            return ANONYMOUS;
        } else {
            return String.valueOf(authentication.getPrincipal());
        }
    }

    /**
     * get device type of request
     * @param request
     * @return
     */
    public static LoginDeviceType getDeviceType(HttpServletRequest request) {
        return isFromMobile(request)?LoginDeviceType.APP:LoginDeviceType.PC;
    }

    /**
     * get session id from request
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
        if(contentLength<0){
            return null;
        }
        byte[] buffer = new byte[contentLength];
        for (int i = 0; i < contentLength;) {
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
}
