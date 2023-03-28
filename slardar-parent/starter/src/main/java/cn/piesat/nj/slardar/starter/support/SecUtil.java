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
import java.util.regex.Pattern;

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

    /**
     * hasRole('') hasAnyRole('','')
     */
    public static final Pattern AUTH_ANNOTATION_PATTERN = Pattern.compile("(\\w+)\\((\\S*)\\)");

    public static final Gson GSON = new GsonBuilder().create();

    public static final String ROLE_NAME_PREFIX = "ROLE_";


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
     * 获取用户登录名
     *
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
     *
     * @param request
     * @return
     */
    public static LoginDeviceType getDeviceType(HttpServletRequest request) {
        return isFromMobile(request) ? LoginDeviceType.APP : LoginDeviceType.PC;
    }


}
