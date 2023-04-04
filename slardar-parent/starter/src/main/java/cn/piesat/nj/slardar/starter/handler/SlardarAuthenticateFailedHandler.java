package cn.piesat.nj.slardar.starter.handler;

import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.support.event.LoginEvent;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static cn.piesat.nj.slardar.starter.support.SecUtil.GSON;

/**
 * 认证失败 handler
 * @author JiajieZhang
 * @date 2022/9/23
 * @description token失效时，自定义返回结果
*/
@Slf4j
public class SlardarAuthenticateFailedHandler implements AuthenticationFailureHandler, AuthenticationEntryPoint {

    private final SlardarContext slardarContext;

    public SlardarAuthenticateFailedHandler(SlardarContext slardarContext) {
        this.slardarContext = slardarContext;
    }


    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
//        response.setContentType("application/json");
//        response.setHeader("Access-Control-Allow-Credentials","true");
//        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
//        response.setHeader("Access-Control-Allow-Headers", "*");
//        response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
//
//        response.setCharacterEncoding("UTF-8");
//        response.getWriter().println(JSONObject.toJSONString(CommonResult.unauthorized(e.getMessage())));
//        response.getWriter().flush();
        onAuthenticationFailure(request, response, e);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        log.error("Authentication failed：{}", e.getLocalizedMessage());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Access-Control-Allow-Credentials","true");
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().println(GSON.toJson(ImmutableMap.of("code", 401, "message", e.getLocalizedMessage())));
        try {
            slardarContext.getEventManager().dispatch(new LoginEvent(request, e));
        } catch (SlardarException ex) {
            ex.printStackTrace();
        }

        response.getWriter().flush();
    }
}
