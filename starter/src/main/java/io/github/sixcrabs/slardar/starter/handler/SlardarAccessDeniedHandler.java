package io.github.sixcrabs.slardar.starter.handler;

import io.github.sixcrabs.slardar.starter.authenticate.SlardarAuthenticateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static io.github.sixcrabs.slardar.starter.support.SecUtil.GSON;

/**
 * 拒绝访问
 * @author JiajieZhang
 * @description 当接口没有访问权限时，自定义返回结果
*/
public class SlardarAccessDeniedHandler implements AccessDeniedHandler {

    private final SlardarAuthenticateService authenticateService;

    public SlardarAccessDeniedHandler(SlardarAuthenticateService authenticateService) {
        this.authenticateService = authenticateService;
    }


    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
        //设置跨域
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setHeader("Access-Control-Allow-Credentials","true");
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, OPTIONS, PATCH");

        response.setCharacterEncoding("UTF-8");
        response.getWriter().println(GSON.toJson(authenticateService.getAuthResultHandler().authDeniedResult(e)));
        response.getWriter().flush();
    }
}