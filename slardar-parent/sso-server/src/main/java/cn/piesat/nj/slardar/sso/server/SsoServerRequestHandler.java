package cn.piesat.nj.slardar.sso.server;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.sso.server.config.SsoServerProperties;
import cn.piesat.nj.slardar.sso.server.support.SsoException;
import cn.piesat.nj.slardar.sso.server.support.SsoHandlerMapping;
import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.SlardarTokenService;
import cn.piesat.nj.slardar.starter.config.SlardarIgnoringCustomizer;
import cn.piesat.nj.slardar.starter.support.SecUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.WebSecurity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import static cn.piesat.nj.slardar.sso.server.SsoConstants.SSO_LOGIN_VIEW_URL;
import static cn.piesat.nj.slardar.sso.server.support.SsoConstants.CODE_20001;
import static cn.piesat.nj.slardar.sso.server.support.SsoConstants.GSON;
import static cn.piesat.nj.slardar.starter.support.HttpServletUtil.forward;
import static cn.piesat.nj.slardar.starter.support.HttpServletUtil.getParam;

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

    private final SlardarTokenService tokenService;

    private final SlardarContext context;

    public SsoServerRequestHandler(SsoServerProperties serverProperties, SlardarContext context) {
        this.serverProperties = serverProperties;
        this.tokenService = context.getBean(SlardarTokenService.class);
        this.context = context;
    }


    public void handle(HttpServletRequest request, HttpServletResponse response) {
        // TODO:
        String uri = request.getRequestURI();
        // 根据不同 path 分发处理
        String mapping = uri.replace(serverProperties.getCtxPath(), "").replaceFirst("/", "");
        SsoHandlerMapping handlerMapping = SsoHandlerMapping.valueOf(mapping);
        switch (handlerMapping) {
            case auth:
                try {
                    handleSsoAuth(request, response);
                } catch (SsoException e) {
                    e.printStackTrace();
                }
                break;
            case logout:
                break;
            case checkTicket:
                handleSsoTicketCheck(request, response);
                break;
            default:
                //
                break;
        }

        getParam(request, "redirect");

        try {
            sendJson(response, HttpStatus.OK, MapUtil.of("code", "1"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    /**
     * TODO
     *
     * @param request
     * @param response
     */
    private void handleSsoTicketCheck(HttpServletRequest request, HttpServletResponse response) {

    }

    /**
     * TODO
     *
     * @param request
     * @param response
     */
    private void handleSsoAuth(HttpServletRequest request, HttpServletResponse response) throws SsoException {
        //
        // ---------- 此处有两种情况分开处理：
        // ---- 情况1：在SSO认证中心尚未登录，需要先去登录
        // TODO: 尝试从请求体里面读取 token
        String tokenValue = tokenService.getTokenValue(request);
        if (StrUtil.isEmpty(tokenValue)) {
            // token 为空 则 跳转到 登录页(登录页面由 认证中心提供)
            try {
                forward(request, response, SSO_LOGIN_VIEW_URL);
            } catch (SlardarException e) {
                throw new SsoException(e).setCode(CODE_20001);
            }
        }
        // FIXME: 验证 token 是否有效
        tokenService.isExpired(tokenValue, SecUtil.getDeviceType(request));
        // TODO: 情况2：在SSO认证中心已经登录，需要重定向回 Client 端
        // 生成 ticket, 带着ticket参数重定向回Client端
        String redirectUrl = ""; //ssoTemplate.buildRedirectUrl(stpLogic.getLoginId(), request.getParam(paramName.client), req.getParam(paramName.redirect));
        try {
            response.sendRedirect(redirectUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }



    /**
     * send json to response
     *
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
        // 忽略 /sso-login
        configure.antMatchers(SSO_LOGIN_VIEW_URL);
    }
}
