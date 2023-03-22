package cn.piesat.nj.slardar.sso.server;

import cn.hutool.core.map.MapUtil;
import cn.piesat.nj.slardar.sso.server.config.SsoServerProperties;
import cn.piesat.nj.slardar.sso.server.support.HttpServletUtil;
import cn.piesat.nj.slardar.sso.server.support.SsoHandlerMapping;
import cn.piesat.nj.slardar.starter.config.SlardarIgnoringCustomizer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.WebSecurity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import static cn.piesat.nj.slardar.sso.server.support.SsoConstants.GSON;

/**
 * <p>
 *     TODO:
 * .处理sso server 端各类请求
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
public class SsoServerRequestHandler implements SlardarIgnoringCustomizer {

    private final SsoServerProperties serverProperties;

    public SsoServerRequestHandler(SsoServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }


    public void handle(HttpServletRequest request, HttpServletResponse response) {
        // TODO:
        String uri = request.getRequestURI();
        // 根据不同 path 分发处理
        String mapping = uri.replace(serverProperties.getCtxPath(), "").replaceFirst("/", "");
        SsoHandlerMapping handlerMapping = SsoHandlerMapping.valueOf(mapping);
        switch (handlerMapping) {
            case auth:
                handleSsoAuth(request, response);
                break;
            case login:
                break;
            case logout:
                break;
            case checkTicket:
                handleSsoTicketCheck(request,response);
                break;
            default:
                //
                break;
        }

        HttpServletUtil.getParam(request, "redirect");

        try {
            sendJson(response, HttpStatus.OK, MapUtil.of("code", "1"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    /**
     * TODO
     * @param request
     * @param response
     */
    private void handleSsoTicketCheck(HttpServletRequest request, HttpServletResponse response) {

    }

    /**
     * TODO
     * @param request
     * @param response
     */
    private void handleSsoAuth(HttpServletRequest request, HttpServletResponse response) {
        //
        // ---------- 此处有两种情况分开处理：
        // ---- 情况1：在SSO认证中心尚未登录，需要先去登录
//        if(stpLogic.isLogin() == false) {
//            return cfg.getNotLoginView().get();
//        }
//        // ---- 情况2：在SSO认证中心已经登录，需要重定向回 Client 端，而这又分为两种方式：
//        String mode = req.getParam(paramName.mode, "");
//        // 方式2：带着ticket参数重定向回Client端 (mode=ticket)
//        String redirectUrl = ssoTemplate.buildRedirectUrl(stpLogic.getLoginId(), request.getParam(paramName.client), req.getParam(paramName.redirect));
//        try {
//            response.sendRedirect(redirectUrl);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

    }


    /**
     * send json to response
     * @param response
     * @param httpStatus
     * @param result
     * @throws IOException
     */
    private void sendJson(HttpServletResponse response, HttpStatus httpStatus, Serializable result) throws IOException {
        response.setStatus(httpStatus.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(GSON.toJson(result));
        response.getWriter().flush();
    }

    @Override
    public void customize(WebSecurity.IgnoredRequestConfigurer configure) {
        // 忽略 `/sso` 相关的 url pattern
        configure.antMatchers(serverProperties.getSsoAntUrlPattern());
    }
}
