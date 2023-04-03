package cn.piesat.nj.slardar.starter.handler;

import cn.piesat.nj.skv.util.MapUtil;
import cn.piesat.nj.slardar.core.AccountInfoDTO;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.core.SlardarSecurityHelper;
import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.SlardarTokenService;
import cn.piesat.nj.slardar.starter.SlardarUserDetails;
import cn.piesat.nj.slardar.starter.config.SlardarProperties;
import cn.piesat.nj.slardar.starter.support.LoginDeviceType;
import cn.piesat.nj.slardar.starter.support.SlardarAuthenticationToken;
import cn.piesat.nj.slardar.starter.support.event.LoginEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static cn.piesat.nj.slardar.core.Constants.DATE_TIME_PATTERN;
import static cn.piesat.nj.slardar.starter.support.HttpServletUtil.isFromMobile;
import static cn.piesat.nj.slardar.starter.support.SecUtil.getAccount;

/**
 * <p>
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

    private static ObjectMapper globalObjectMapper = new ObjectMapper();

    private final SlardarProperties securityProperties;

    private final SlardarTokenService tokenService;

    private final SlardarContext context;

    static {
        globalObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // 处理LocalDateTime
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        globalObjectMapper.registerModule(javaTimeModule);
    }

    public SlardarAuthenticateSucceedHandler(SlardarProperties securityProperties, SlardarTokenService tokenService, SlardarContext context) {
        this.securityProperties = securityProperties;
        this.tokenService = tokenService;
        this.context = context;
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
        SlardarAuthenticationToken authenticationToken = (SlardarAuthenticationToken) authentication;
        SlardarUserDetails userDetails = authenticationToken.getUserDetails();
        //获取token,将token存储到redis中
        String token = tokenService.createToken(String.valueOf(authenticationToken.getPrincipal()),
                isFromMobile(request) ? LoginDeviceType.APP : LoginDeviceType.PC, securityProperties.getLogin().getConcurrentPolicy());
        // 设置登录状态
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        // set context holder
        SecurityContextHolder.getContext().setAuthentication(authentication);
        SlardarSecurityHelper.getContext()
                .setAccount(userDetails.getAccount())
                .setUserProfile(userDetails.getAccount().getUserProfile());
        response.setStatus(HttpStatus.OK.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // store token
        tokenService.setTokenValue(token, request, response);
        AccountInfoDTO accountInfoDTO = new AccountInfoDTO().setAccountName(userDetails.getAccount().getName())
                .setAccountNonExpired(false).setAccountNonLocked(false)
                .setToken(token)
                .setUserProfile(userDetails.getAccount().getUserProfile())
                .setOpenId(userDetails.getAccount().getOpenId());
        Map<String, Object> res = MapUtil.of("data", accountInfoDTO);
        res.put("code", securityProperties.getLogin().getLoginSuccessCode());
        globalObjectMapper.writeValue(response.getWriter(), res);
        clearAuthenticationAttributes(request);
        try {
            context.getEventManager().dispatch(new LoginEvent(getAccount(), true));
        } catch (SlardarException e) {
            e.printStackTrace();
        }
    }


    private static void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }
}
