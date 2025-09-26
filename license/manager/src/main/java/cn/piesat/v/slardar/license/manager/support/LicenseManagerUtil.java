package cn.piesat.v.slardar.license.manager.support;

import cn.piesat.v.misc.hutool.mini.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static cn.piesat.v.slardar.starter.support.SecUtil.GSON;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/9/26
 */
public final class LicenseManagerUtil {

    private static final Logger log = LoggerFactory.getLogger(LicenseManagerUtil.class);

    private LicenseManagerUtil() {
    }

    /**
     * send json to response
     *
     * @param response
     * @param result
     * @throws IOException
     */
    public static void sendJson(HttpServletResponse response, HttpStatus httpStatus, Serializable result) {
        response.setStatus(httpStatus.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String message = "Unknown error...";
        if (result != null) {
            if (result instanceof Exception) {
                message = ((Exception) result).getMessage();
            } else {
                message = (result instanceof String) ? result.toString() : GSON.toJson(result);
            }
        }
        try {
            response.getWriter().write(message);
            response.getWriter().flush();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public static HashMap<String, Object> makeResult(Object result, int code) {
        HashMap<String, Object> ret = MapUtil.of("code", code);
        ret.put("data", result);
        return ret;
    }

    public static HashMap<String, Object> makeErrorResult(String msg, int code) {
        HashMap<String, Object> ret = MapUtil.of("code", code);
        ret.put("data", null);
        ret.put("message", msg);
        return ret;
    }
}
