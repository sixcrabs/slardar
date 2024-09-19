package cn.piesat.nj.slardar.starter.handler;

import cn.hutool.core.map.MapUtil;
import cn.piesat.nj.slardar.core.Constants;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.spi.SlardarSpiContext;
import cn.piesat.nj.slardar.starter.SlardarAuthenticateService;
import cn.piesat.nj.slardar.starter.SlardarEventManager;
import cn.piesat.nj.slardar.starter.authenticate.SlardarAuthentication;
import cn.piesat.nj.slardar.starter.authenticate.mfa.MfaVerifyRequiredException;
import cn.piesat.nj.slardar.starter.support.SlardarAuthenticationException;
import cn.piesat.nj.slardar.starter.support.event.LoginEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

import static cn.piesat.nj.slardar.starter.support.HttpServletUtil.*;

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

    private static final Logger log = LoggerFactory.getLogger(SlardarAuthenticateFailedHandler.class);

    public SlardarAuthenticateFailedHandler(SlardarSpiContext slardarContext, SlardarAuthenticateService authenticateService) {
        this.slardarContext = slardarContext;
        this.authenticateService = authenticateService;
    }


    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        onAuthenticationFailure(request, response, e);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        HashMap<String, Object> resp;
        HttpStatus status = (e instanceof AuthenticationServiceException || e instanceof UsernameNotFoundException) ?
                HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.UNAUTHORIZED;
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
}
