package cn.piesat.v.slardar.starter.handler;

import cn.hutool.core.map.MapUtil;
import cn.piesat.v.slardar.core.Constants;
import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.starter.SlardarAuthenticateService;
import cn.piesat.v.slardar.starter.SlardarEventManager;
import cn.piesat.v.slardar.starter.authenticate.SlardarAuthentication;
import cn.piesat.v.slardar.starter.authenticate.mfa.MfaVerifyRequiredException;
import cn.piesat.v.slardar.starter.config.SlardarProperties;
import cn.piesat.v.slardar.starter.support.SlardarAuthenticationException;
import cn.piesat.v.slardar.starter.support.event.LoginEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

import static cn.piesat.v.slardar.starter.support.HttpServletUtil.*;

/**
 * 认证失败 handler
 *
 * @author JiajieZhang
 * @date 2022/9/23
 * @description token失效时，自定义返回结果
 */
public class SlardarAuthenticateFailedHandler implements AuthenticationFailureHandler, AuthenticationEntryPoint {

    private final SlardarSpiContext slardarContext;

    private final SlardarAuthenticateService authenticateService;

    private final SlardarProperties properties;

    private static final Logger log = LoggerFactory.getLogger(SlardarAuthenticateFailedHandler.class);

    public SlardarAuthenticateFailedHandler(SlardarSpiContext slardarContext, SlardarAuthenticateService authenticateService) {
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
                                            .setReqClientIp(geRequestIpAddress(request)), getHeadersAsMap(request), e));
                } catch (SlardarException ex) {
                    log.error(ex.getLocalizedMessage());
                }
            }
        }
        sendJson(response, resp, status, request.getHeader("Origin"));
    }

    @Nonnull
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
