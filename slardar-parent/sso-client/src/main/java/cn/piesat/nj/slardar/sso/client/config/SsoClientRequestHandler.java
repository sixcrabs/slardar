package cn.piesat.nj.slardar.sso.client.config;

import cn.hutool.core.map.MapUtil;
import cn.piesat.nj.slardar.sso.client.config.support.SsoClientHandlerMapping;
import cn.piesat.nj.slardar.sso.client.config.support.SsoException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * <p>
 * .
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/30
 */
public class SsoClientRequestHandler {

    public static final Logger log = LoggerFactory.getLogger(SsoClientRequestHandler.class);

    public static final Gson GSON = new GsonBuilder().create();

    private final SsoClientProperties clientProperties;

    public SsoClientRequestHandler(SsoClientProperties clientProperties) {
        this.clientProperties = clientProperties;
    }

    /**
     * TODO
     * 处理 sso-client 请求
     * @param request
     * @param response
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        // 根据不同 path 分发处理
        String mapping = uri.replace(clientProperties.getCtxPath(), "").replaceFirst("/", "");
        try {
            switch (SsoClientHandlerMapping.valueOf(mapping)) {
                case isLogin:
                    // TODO /sso/userdetail
                    break;
                case doLogout:
                    // TODO /sso/logout
                    break;
                case getSsoAuthUrl:
                    // TODO
                    // 返回 /sso/auth

                    break;
                case doLogin:
                    // TODO
                    // 使用ticket 登录
                    // /sso/checkTicket
                    // 登录成功后 写入 contextHolder
                    break;
                default:
                    //
                    throw new SsoException("error");
            }
        } catch (SsoException e) {
            sendJson(response, e.toString(), HttpStatus.OK);
        }
    }



    /**
     * send json to response
     *
     * @param response
     * @param result
     * @throws IOException
     */
    private void sendJson(HttpServletResponse response, Serializable result, HttpStatus httpStatus) {
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

    private HashMap<String, Object> makeResult(Object result, int code) {
        HashMap<String, Object> ret = MapUtil.of("code", code);
        ret.put("data", result);
        return ret;
    }
}
