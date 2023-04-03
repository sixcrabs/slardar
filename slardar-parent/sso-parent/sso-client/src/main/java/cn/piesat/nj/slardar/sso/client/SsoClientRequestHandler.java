package cn.piesat.nj.slardar.sso.client;

import cn.hutool.core.map.MapUtil;
import cn.piesat.nj.slardar.sso.client.config.SsoClientProperties;
import cn.piesat.nj.slardar.sso.client.config.client.SsoServerClient;
import cn.piesat.nj.slardar.sso.client.support.SsoClientHandlerMapping;
import cn.piesat.nj.slardar.sso.client.support.SsoException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static cn.piesat.nj.slardar.sso.client.support.HttpServletUtil.getCookieValue;
import static cn.piesat.nj.slardar.sso.client.support.HttpServletUtil.getParam;

/**
 * <p>
 * .
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/30
 */
public class SsoClientRequestHandler {

    private static final Logger log = LoggerFactory.getLogger(SsoClientRequestHandler.class);

    private static final Gson GSON = new GsonBuilder().create();

    private final SsoClientProperties clientProperties;

    @Resource
    private SsoServerClient serverClient;

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
                    // 使用ticket 登录
                    // 登录成功后 进行跳转
                    doLoginByTicket(request, response);
                    break;
                default:
                    //
                    throw new SsoException("error");
            }
        } catch (SsoException e) {
            sendJson(response, e.toString(), HttpStatus.OK);
        }
    }

    private void doLoginByTicket(HttpServletRequest request, HttpServletResponse response) {
        // 通过 rest api向 sso server 验证 ticket
        String ticket = getParam(request, "ticket");
        serverClient.checkTicket(ticket);




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
