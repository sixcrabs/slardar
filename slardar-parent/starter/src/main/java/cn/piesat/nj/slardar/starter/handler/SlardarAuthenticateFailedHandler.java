package cn.piesat.nj.slardar.starter.handler;

import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.authenticate.mfa.MfaVerifyRequiredException;
import cn.piesat.nj.slardar.starter.support.event.LoginEvent;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static cn.piesat.nj.slardar.starter.support.SecUtil.GSON;

/**
 * 认证失败 handler
 *
 * @author JiajieZhang
 * @date 2022/9/23
 * @description token失效时，自定义返回结果
 */
public class SlardarAuthenticateFailedHandler implements AuthenticationFailureHandler, AuthenticationEntryPoint {

    private final SlardarContext slardarContext;

    private static final Logger log = LoggerFactory.getLogger(SlardarAuthenticateFailedHandler.class);

    public SlardarAuthenticateFailedHandler(SlardarContext slardarContext) {
        this.slardarContext = slardarContext;
    }


    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        onAuthenticationFailure(request, response, e);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        Map<String, Object> resp = new HashMap<>(1);
        HttpStatus status = (e instanceof AuthenticationServiceException || e instanceof UsernameNotFoundException) ?
                HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.UNAUTHORIZED;
        if (e instanceof MfaVerifyRequiredException) {
            // 单独处理MFA 异常
            resp.put("code", 1008);
            resp.put("key", ((MfaVerifyRequiredException) e).getKey());
            resp.put("message", "MFA authentication required!");
        } else {
            String errMsg = e.getLocalizedMessage();
            log.error("Authentication failed：{}", errMsg);
            resp.put("code", status.value());
            resp.put("message", Objects.isNull(errMsg) ? "Null" : errMsg);
            try {
                slardarContext.getEventManager().dispatch(new LoginEvent(request, e));
            } catch (SlardarException ex) {
                log.error(ex.getLocalizedMessage());
            }
        }
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().println(GSON.toJson(resp));
        response.getWriter().flush();
    }
}
