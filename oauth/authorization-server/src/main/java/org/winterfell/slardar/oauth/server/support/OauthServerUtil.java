package org.winterfell.slardar.oauth.server.support;

import cn.piesat.v.misc.hutool.mini.MapUtil;
import org.winterfell.slardar.core.SlardarException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.winterfell.slardar.starter.support.HttpServletUtil.forward;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/4/13
 */
public final class OauthServerUtil {

    public static final Logger log = LoggerFactory.getLogger(OauthServerUtil.class);

    public static final Gson GSON = new GsonBuilder().create();

    /**
     * 默认的oauth2请求的上下文路径 可以通过配置文件进行修改
     */
    public static final String OAUTH_CTX_PATH = "/oauth2";

    /**
     * 授权码模式
     */
    public static final String RESPONSE_TYPE_CODE = "code";

    /**
     * 隐式模式
     */
    public static final String RESPONSE_TYPE_IMPLICIT = "token";

    /**
     * 密码模式
     */
    public static final String GRANT_TYPE_PASSWORD = "password";

    /**
     * 客户端模式
     */
    public static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";


    /**
     * 转发失败
     */
    public static final int CODE_FORWARD_FAILED = 30001;

    /**
     * 重定向失败
     */
    public static final int CODE_REDIRECT_FAILED = 30002;


    /**
     * send forward
     *
     * @param request
     * @param response
     * @param path
     */
    public static void sendForward(HttpServletRequest request, HttpServletResponse response, String path) {
        try {
            // token 为空 则 跳转到 登录页(登录页面由 认证中心提供)
            forward(request, response, path);
        } catch (SlardarException e) {
            log.error(e.getLocalizedMessage());
        }
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

    /**
     * 验证URL的正则表达式
     */
    private static final String URL_REGEX = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";

    /**
     * 校验重定向url合法性
     *
     * @param url redirect to
     */
    public static String resolveRedirectUrl(String url) throws OauthServerException {
        if (!StringUtils.hasText(url)) {
            throw new OauthServerException("重定向地址[url]为空").setCode(CODE_REDIRECT_FAILED);
        }
        if (!url.toLowerCase().matches(URL_REGEX)) {
            throw new OauthServerException("重定向地址无效：" + url).setCode(CODE_REDIRECT_FAILED);
        }
        // 截取掉?后面的部分
        int idx = url.indexOf("?");
        if (idx != -1) {
            return url.substring(0, idx);
        }
        // TODO: 地址限制等
        return url;
    }


}
