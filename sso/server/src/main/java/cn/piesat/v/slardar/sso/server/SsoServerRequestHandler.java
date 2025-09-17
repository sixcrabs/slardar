package cn.piesat.v.slardar.sso.server;

import cn.piesat.v.misc.hutool.mini.MapUtil;
import cn.piesat.v.misc.hutool.mini.StringUtil;
import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.core.entity.Account;
import cn.piesat.v.slardar.core.entity.UserProfile;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.sso.server.config.SlardarSsoUserDetailsHandler;
import cn.piesat.v.slardar.sso.server.config.SsoServerProperties;
import cn.piesat.v.slardar.sso.server.support.SsoException;
import cn.piesat.v.slardar.sso.server.support.SsoHandlerMapping;
import cn.piesat.v.slardar.starter.SlardarAuthenticateService;
import cn.piesat.v.slardar.starter.SlardarUserDetails;
import cn.piesat.v.slardar.starter.config.SlardarIgnoringCustomizer;
import cn.piesat.v.slardar.starter.support.LoginDeviceType;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static cn.piesat.v.slardar.sso.server.support.SsoConstants.*;
import static cn.piesat.v.slardar.starter.support.HttpServletUtil.*;

/**
 * <p>
 * TODO:
 * .处理sso server 端各类请求
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
public class SsoServerRequestHandler implements SlardarIgnoringCustomizer, SlardarSsoUserDetailsHandler {

    private static final Logger log = LoggerFactory.getLogger(SsoServerRequestHandler.class);

    private final SsoServerProperties serverProperties;

    private final SlardarAuthenticateService tokenService;

    private final SlardarSpiContext context;

    private final SsoTicketService ticketService;

    @Resource
    private UserDetailsService userDetailsService;

    public SsoServerRequestHandler(SsoServerProperties serverProperties,
                                   SlardarSpiContext context,
                                   SsoTicketService ticketService) {
        this.serverProperties = serverProperties;
        this.tokenService = context.getBean(SlardarAuthenticateService.class);
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
                    // TODO: sso 登出
                    break;
                case checkTicket:
                case checkticket:
                    handleSsoTicketCheck(request, response);
                    break;
                case userDetails:
                case userdetails:
                    handleUserDetails(request, response);
                    break;
                case validatetoken:
                case validateToken:
                    // TODO:
                    handleValidate(request, response);
                    break;
                default:
                    break;
            }
        } catch (SsoException e) {
            sendJson(response, makeErrorResult(e.getCauseMessage(), e.getCode() > 0 ? e.getCode() : 4001), HttpStatus.OK);
        }
    }

    /**
     * token 验证
     *
     * @param request
     * @param response
     */
    private void handleValidate(HttpServletRequest request, HttpServletResponse response) throws SsoException {
        String tokenValue = tokenService.getTokenValueFromServlet(request);
        if (StringUtil.isBlank(tokenValue)) {
            throw new SsoException("Token is required").setCode(401);
        }
        LoginDeviceType deviceType = getDeviceType(request);
        boolean expired = tokenService.isExpired(tokenValue, deviceType);
        if (expired) {
            throw new SsoException("Token is expired").setCode(401);
        } else {
            sendJson(response, makeResult(ImmutableMap.of("isValid", true, "ttl", tokenService.ttl(tokenValue, deviceType)),
                    HttpStatus.OK.value()), HttpStatus.OK);
        }
    }

    /**
     * 返回当前已登录用户详情
     *
     * @param request
     * @param response
     */
    private void handleUserDetails(HttpServletRequest request, HttpServletResponse response) throws SsoException {
        String tokenValue = tokenService.getTokenValueFromServlet(request);
        if (StringUtil.isBlank(tokenValue)) {
            throw new SsoException("Token is required").setCode(401);
        }
        boolean expired = tokenService.isExpired(tokenValue, getDeviceType(request));
        if (expired) {
            throw new SsoException("Token is expired").setCode(401);
        } else {
            try {
                // get user details
                String username = tokenService.getUsernameFromTokenValue(tokenValue);
                SlardarUserDetails details = (SlardarUserDetails) userDetailsService.loadUserByUsername(username);
                Collection<SlardarSsoUserDetailsHandler> ssoUserDetailsHandlers = context.getBeans(SlardarSsoUserDetailsHandler.class);
                if (!ssoUserDetailsHandlers.isEmpty()) {
                    List<Serializable> list = ssoUserDetailsHandlers.stream().map(handler -> {
                        try {
                            return handler.handle(details);
                        } catch (SsoException e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());
                    if (!list.isEmpty()) {
                        sendJson(response, makeResult(list.get(list.size() - 1), HttpStatus.OK.value()), HttpStatus.OK);
                    } else {
                        sendJson(response, makeResult(details.getAccount(), HttpStatus.OK.value()), HttpStatus.OK);
                    }
                } else {
                    sendJson(response, makeResult(details.getAccount(), HttpStatus.OK.value()), HttpStatus.OK);
                }

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
            if (!StringUtils.hasText(token)) {
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
        String tokenValue = tokenService.getTokenValueFromServlet(request);
        if (StringUtil.isBlank(tokenValue)) {
            // 跳转到SSO登录页
            sendForward(request, response, SSO_LOGIN_VIEW_URL);
        }
        boolean expired = tokenService.isExpired(tokenValue, getDeviceType(request));
        if (expired) {
            sendForward(request, response, SSO_LOGIN_VIEW_URL);
        }
        // 在SSO认证中心已经登录，需要重定向回 Client 端 /sso/auth?url=http://client.com/xxxx
        String redirectUrl = getParam(request, SSO_PARAM_REDIRECT);
        redirectUrl = resolveRedirectUrl(redirectUrl);
        // 生成 ticket, 带着ticket参数重定向回Client端
        // FIXME: 这里需要根据 token 生成  而不是：getSessionId(request)
        String ticket = ticketService.createTicket(tokenValue);
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
    public String resolveRedirectUrl(String url) throws SsoException {
        if (!StringUtils.hasText(url)) {
            throw new SsoException("重定向地址[url]为空").setCode(CODE_20002);
        }
        if (!url.toLowerCase().matches(URL_REGEX)) {
            throw new SsoException("重定向地址无效：" + url).setCode(CODE_20002);
        }
        // 截取掉?后面的部分
        int idx = url.indexOf("?");
        if (idx != -1) {
            return url.substring(0, idx);
        }
        // TODO: 地址限制等
        return url;
    }

    /**
     * 处理用户详情，如: 屏蔽密码 或修改返回结构等
     *
     * @param details
     * @return
     * @throws SlardarException
     */
    @Override
    public Serializable handle(SlardarUserDetails details) throws SsoException {
        Account account = details.getAccount();
        UserProfile userProfile = account.setPassword(null).getUserProfile();
        if (userProfile != null) {
            userProfile.setAuthorities(Collections.emptyList());
        }
        return account;
    }

    /**
     * 自定义过滤需要忽略的url
     *
     * @param antPatterns
     */
    @Override
    public void customize(List<String> antPatterns) {
        // 忽略 `/sso` 相关的 url pattern
        antPatterns.add(serverProperties.getSsoAntUrlPattern());
        // 忽略 /sso-login
        antPatterns.add(SSO_LOGIN_VIEW_URL);
    }
}
