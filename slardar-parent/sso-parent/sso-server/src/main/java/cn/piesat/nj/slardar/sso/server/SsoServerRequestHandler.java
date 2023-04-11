package cn.piesat.nj.slardar.sso.server;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.sso.server.config.SsoServerProperties;
import cn.piesat.nj.slardar.sso.server.support.SsoException;
import cn.piesat.nj.slardar.sso.server.support.SsoHandlerMapping;
import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.SlardarTokenService;
import cn.piesat.nj.slardar.starter.SlardarUserDetails;
import cn.piesat.nj.slardar.starter.config.SlardarIgnoringCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static cn.piesat.nj.slardar.sso.server.SsoConstants.SSO_LOGIN_VIEW_URL;
import static cn.piesat.nj.slardar.sso.server.support.SsoConstants.*;
import static cn.piesat.nj.slardar.starter.support.HttpServletUtil.*;

/**
 * <p>
 * TODO:
 * .处理sso server 端各类请求
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
public class SsoServerRequestHandler implements SlardarIgnoringCustomizer {

    private static final Logger log = LoggerFactory.getLogger(SsoServerRequestHandler.class);

    private final SsoServerProperties serverProperties;

    private final SlardarTokenService tokenService;

    private final SlardarContext context;

    private final SsoTicketService ticketService;

    @Resource
    private UserDetailsService userDetailsService;

    public SsoServerRequestHandler(SsoServerProperties serverProperties, SlardarContext context, SsoTicketService ticketService) {
        this.serverProperties = serverProperties;
        this.tokenService = context.getBean(SlardarTokenService.class);
        this.context = context;
        this.ticketService = ticketService;
    }


    /**
     * handle sso server method
     *
     * @param request
     * @param response
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        // 根据不同 path 分发处理
        String mapping = uri.replace(serverProperties.getCtxPath(), "").replaceFirst("/", "");
        SsoHandlerMapping handlerMapping = SsoHandlerMapping.valueOf(mapping);
        try {
            switch (handlerMapping) {
                case auth:
                    handleSsoAuth(request, response);
                    break;
                case logout:
                    // TODO
                    break;
                case checkTicket:
                    handleSsoTicketCheck(request, response);
                    break;
                case userdetails:
                    handleUserDetails(request, response);
                    break;
                default:
                    break;
            }
        } catch (SsoException e) {
            sendJson(response, makeErrorResult(e.getLocalizedMessage(), e.getCode() > 0 ? e.getCode() : 4001), HttpStatus.OK);
        }
    }

    /**
     * 返回当前已登录用户详情
     *
     * @param request
     * @param response
     */
    private void handleUserDetails(HttpServletRequest request, HttpServletResponse response) throws SsoException {
        String tokenValue = tokenService.getTokenValue(request);
        if (StrUtil.isEmpty(tokenValue)) {
            throw new SsoException("Token is required").setCode(401);
        }
        boolean expired = tokenService.isExpired(tokenValue, getDeviceType(request));
        if (expired) {
            throw new SsoException("Token is expired").setCode(401);
        } else {
            try {
                // get user details
                String username = tokenService.getUsername(tokenValue);
                SlardarUserDetails details = (SlardarUserDetails) userDetailsService.loadUserByUsername(username);
                if (details.getAccount() != null) {
                    details.getAccount().setPassword("");
                }
                sendJson(response, makeResult(details.getAccount(), HttpStatus.OK.value()), HttpStatus.OK);
            } catch (UsernameNotFoundException e) {
                throw new SsoException(e.getLocalizedMessage()).setCode(HttpStatus.SERVICE_UNAVAILABLE.value());
            }
        }
    }

    /**
     * 校验 ticket 并返回 token信息
     *
     * @param request
     * @param response
     */
    private void handleSsoTicketCheck(HttpServletRequest request, HttpServletResponse response) throws SsoException {
        String ticketValue = getParam(request, SSO_PARAM_TICKET);
        try {
            String token = ticketService.checkTicket(ticketValue);
            if (StringUtils.isEmpty(token)) {
                throw new SsoException("Ticket 验证失败: 已过期").setCode(CODE_TICKET_ERROR);
            }
            sendJson(response, makeResult(token, 0), HttpStatus.OK);
        } catch (Exception e) {
            throw new SsoException("Ticket 验证失败: " + e.getLocalizedMessage()).setCode(CODE_TICKET_ERROR);
        }
    }

    /**
     * 处理 /sso/auth 请求
     * - 验证当前身份
     * - 做出跳转
     *
     * @param request
     * @param response
     */
    private void handleSsoAuth(HttpServletRequest request, HttpServletResponse response) throws SsoException {
        // 尝试从请求里面读取 token 并验证是否有效
        String tokenValue = tokenService.getTokenValue(request);
        if (StrUtil.isEmpty(tokenValue)) {
            // 跳转到SSO登录页
            sendForward(request, response, SSO_LOGIN_VIEW_URL);
        }
        boolean expired = tokenService.isExpired(tokenValue, getDeviceType(request));
        if (expired) {
            sendForward(request, response, SSO_LOGIN_VIEW_URL);
        }
        // 在SSO认证中心已经登录，需要重定向回 Client 端 /ss/auth?url=http://client.com/xxxx
        String redirectUrl = getParam(request, SSO_PARAM_REDIRECT);
        validateRedirectUrl(redirectUrl);
        // 生成 ticket, 带着ticket参数重定向回Client端
        String ticket = ticketService.createTicket(getSessionId(request));
        try {
            response.sendRedirect(redirectUrl.concat("?ticket=").concat(ticket));
        } catch (IOException e) {
            throw new SsoException(e.getLocalizedMessage()).setCode(CODE_20002);
        }
    }

    /**
     * send forward
     *
     * @param request
     * @param response
     * @param path
     */
    private void sendForward(HttpServletRequest request, HttpServletResponse response, String path) {
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

    private HashMap<String, Object> makeErrorResult(String msg, int code) {
        HashMap<String, Object> ret = MapUtil.of("code", code);
        ret.put("data", new Object());
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
    public void validateRedirectUrl(String url) throws SsoException {
        if (StringUtils.isEmpty(url)) {
            throw new SsoException("重定向地址为空").setCode(CODE_20002);
        }
        if (!url.toLowerCase().matches(URL_REGEX)) {
            throw new SsoException("无效redirect：" + url).setCode(CODE_20002);
        }
        // 截取掉?后面的部分
        int idx = url.indexOf("?");
        if (idx != -1) {
            url = url.substring(0, idx);
        }
        // TODO: 地址限制等
        return;
    }

    @Override
    public void customize(WebSecurity.IgnoredRequestConfigurer configure) {
        // 忽略 `/sso` 相关的 url pattern
        configure.antMatchers(serverProperties.getSsoAntUrlPattern());
        // 忽略 /sso-login
        configure.antMatchers(SSO_LOGIN_VIEW_URL);
    }
}
