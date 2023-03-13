package cn.piesat.nj.slardar.starter.handler;

import cn.piesat.nj.slardar.starter.config.SlardarProperties;
import cn.piesat.v.authx.security.infrastructure.spring.SecurityProperties;
import cn.piesat.v.authx.security.infrastructure.spring.support.AuthxAuthentication;
import cn.piesat.v.authx.security.infrastructure.spring.support.LoginDeviceType;
import cn.piesat.v.authx.security.infrastructure.spring.token.AuthxTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static cn.piesat.v.authx.security.infrastructure.spring.support.SecUtil.isFromMobile;

/**
 * <p>
 *     TODO:
 * 认证成功 handler
 * - 写入 jwt value
 * - 更新用户审计信息（如：记录用户登录时刻等）
 * - 其他
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/26
 */
@Slf4j
public class SlardarAuthenticateSucceedHandler implements AuthenticationSuccessHandler {


    private final SlardarProperties securityProperties;

    @Autowired
    private AuthxTokenService tokenService;

    public SlardarAuthenticateSucceedHandler(SlardarProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    /**
     * 生成 token
     * - 根据账户的策略和系统配置的策略 决定 token 是否共用
     *
     * @param request
     * @param response
     * @param authentication
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // TESTME 生成 jwt 等
        AuthxAuthentication authzAuthentication = (AuthxAuthentication) authentication;
        //获取token,将token存储到redis中
        //判断登录类型
        String token = tokenService.createToken(String.valueOf(authzAuthentication.getPrincipal()),
                isFromMobile(request) ? LoginDeviceType.APP : LoginDeviceType.PC, securityProperties.getLogin().getConcurrentPolicy());
//        //获取菜单
//        List<UcMenuTreeCO> menuList = ucMenuGateway.selectMenuTreeByAccount(authzAuthentication.getUserDetails().getUsername(),
//                CommonConstant.ROLE_OPEN_STATUS);
//
//        UcAccountLoginCO loginCO = new UcAccountLoginCO();
//        loginCO.setToken(token);
//        loginCO.setMenu(menuList);
//        loginCO.setAccount(authzAuthentication.getUserDetails().getAccount().getAccount());
//        loginCO.setPhoto(authzAuthentication.getUserDetails().getPhoto());
//        loginCO.setName(ObjectUtils.isEmpty(authzAuthentication.getUserDetails().getAccount().getName()) ? "" : authzAuthentication.getUserDetails().getAccount().getName());
//        loginCO.setIsSystemAdmin(authzAuthentication.getUserDetails().getAccount().getIsSystemAdmin());

        // TODO: 设置登录状态
        authzAuthentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        response.setStatus(HttpStatus.OK.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        response.getWriter().write(GSON.toJSONString());
        response.getWriter().flush();
        clearAuthenticationAttributes(request);
        // TODO: 记录用户的登录时间
    }

    private static void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }
}
