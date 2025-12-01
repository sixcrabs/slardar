package org.winterfell.slardar.starter.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.winterfell.misc.hutool.mini.MapUtil;
import org.winterfell.slardar.core.Constants;
import org.winterfell.slardar.core.SlardarException;
import org.winterfell.slardar.core.SlardarContext;
import org.winterfell.slardar.starter.authenticate.SlardarAuthenticateService;
import org.winterfell.slardar.starter.SlardarEventManager;
import org.winterfell.slardar.starter.authenticate.SlardarAuthentication;
import org.winterfell.slardar.starter.authenticate.mfa.MfaVerifyRequiredException;
import org.winterfell.slardar.starter.SlardarProperties;
import org.winterfell.slardar.starter.support.SlardarAuthenticationException;
import org.winterfell.slardar.starter.support.event.LoginEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.util.HashMap;

import static org.winterfell.slardar.starter.support.HttpServletUtil.*;

/**
 * 认证失败 handler
 *
 * @author JiajieZhang
 * @version 2022/9/23
 * @description token失效时，自定义返回结果
 */
public class SlardarAuthenticateFailedHandler implements AuthenticationFailureHandler, AuthenticationEntryPoint {

    private final SlardarContext slardarContext;

    private final SlardarAuthenticateService authenticateService;

    private final SlardarProperties properties;

    private static final Logger log = LoggerFactory.getLogger(SlardarAuthenticateFailedHandler.class);

    public SlardarAuthenticateFailedHandler(SlardarContext slardarContext, SlardarAuthenticateService authenticateService) {
        this.slardarContext = slardarContext;
        this.authenticateService = authenticateService;
        this.properties = slardarContext.getBean(SlardarProperties.class);
    }


    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        onAuthenticationFailure(request, response, e);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        HashMap<String, Object> resp;
        HttpStatus status = getHttpStatus(e);
        if (e instanceof MfaVerifyRequiredException) {
            // 单独处理MFA 异常
            resp = makeResult(((MfaVerifyRequiredException) e).getKey(), 1008, "MFA authentication required!");
        } else {
            resp = (HashMap<String, Object>) authenticateService.getAuthResultHandler().authFailedResult(e);
            Integer code = MapUtil.getInt(resp, "code");
            if (code != null && code == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                // 无 token 情况不记录登录日志
                try {
                    String accountName = "";
                    if (e instanceof SlardarAuthenticationException) {
                        // 获取到认证异常的账号名
                        accountName = ((SlardarAuthenticationException) e).getAccountName();
                    }
                    slardarContext.getBeanIfAvailable(SlardarEventManager.class)
                            .dispatch(new LoginEvent(
                                    new SlardarAuthentication(accountName, Constants.AUTH_TYPE_NORMAL, null)
                                            .setLoginDeviceType(getDeviceType(request))
                                            .setReqClientIp(getRequestIpAddress(request)), getHeadersAsMap(request), e));
                } catch (SlardarException ex) {
                    log.error(ex.getLocalizedMessage());
                }
            }
        }
        sendJson(response, resp, status, request.getHeader("Origin"));
    }

    private HttpStatus getHttpStatus(AuthenticationException e) {
        HttpStatus status = (e instanceof AuthenticationServiceException || e instanceof UsernameNotFoundException
                || e instanceof SlardarAuthenticationException) ?
                HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.UNAUTHORIZED;
        int loginFailedHttpStatus = properties.getLogin().getLoginFailedHttpStatus();
        if (status.equals(HttpStatus.INTERNAL_SERVER_ERROR) && loginFailedHttpStatus < 500) {
            status = HttpStatus.valueOf(loginFailedHttpStatus);
        }
        return status;
    }
}