package org.winterfell.slardar.starter.handler;

import org.winterfell.slardar.core.AccountInfoDTO;
import org.winterfell.slardar.core.SlardarException;
import org.winterfell.slardar.core.SlardarSecurityHelper;
import org.winterfell.slardar.core.entity.Account;
import org.winterfell.slardar.spi.SlardarSpiContext;
import org.winterfell.slardar.spi.token.SlardarTokenProvider;
import org.winterfell.slardar.starter.SlardarEventManager;
import org.winterfell.slardar.starter.SlardarAuthenticateService;
import org.winterfell.slardar.starter.SlardarUserDetails;
import org.winterfell.slardar.starter.support.LoginDeviceType;
import org.winterfell.slardar.starter.authenticate.SlardarAuthentication;
import org.winterfell.slardar.starter.support.event.LoginEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
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
import java.util.Collection;

import static org.winterfell.slardar.core.Constants.DATE_TIME_PATTERN;
import static org.winterfell.slardar.starter.support.HttpServletUtil.getHeadersAsMap;
import static org.winterfell.slardar.starter.support.HttpServletUtil.isFromMobile;
import static org.winterfell.slardar.starter.support.SecUtil.getAccount;

/**
 * <p>
 * 认证成功 handler
 * <ul>
 *     <li> 写入 jwt value</li>
 *     <li>更新用户审计信息（如：记录用户登录时刻等）</li>
 *     <li>其他</li>
 * </ul>
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/26
 */
public class SlardarAuthenticateSucceedHandler implements AuthenticationSuccessHandler {

    private static final ObjectMapper globalObjectMapper = new ObjectMapper();

    private final SlardarAuthenticateService authenticateService;

    private final SlardarSpiContext context;

    private static final Logger log = LoggerFactory.getLogger(SlardarAuthenticateSucceedHandler.class);

    static {
        globalObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // 处理LocalDateTime
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        globalObjectMapper.registerModule(javaTimeModule);
    }

    public SlardarAuthenticateSucceedHandler(SlardarAuthenticateService authenticateService, SlardarSpiContext context) {
        this.authenticateService = authenticateService;
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
        SlardarAuthentication authenticationToken = (SlardarAuthentication) authentication;
        SlardarUserDetails userDetails = authenticationToken.getUserDetails();
        //获取token,将token存储到redis中
        SlardarTokenProvider.SlardarToken tokenSlardarToken = authenticateService.createToken(String.valueOf(authenticationToken.getPrincipal()),
                isFromMobile(request) ? LoginDeviceType.APP : LoginDeviceType.PC);
        // 设置登录状态
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        // set context holder
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Account account = userDetails.getAccount();
        SlardarSecurityHelper.getContext()
                .setAccount(account)
                .setUserProfile(account.getUserProfile());
        response.setStatus(HttpStatus.OK.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // store token value
        authenticateService.setTokenValueIntoServlet(tokenSlardarToken.getTokenValue(), request, response);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        AccountInfoDTO accountInfoDTO = new AccountInfoDTO()
                .setAccountName(account.getName())
                .setAccountExpired(account.isExpired())
                .setAccountLocked(account.isLocked())
                .setToken(tokenSlardarToken.getTokenValue())
                .setTokenExpiresAt(tokenSlardarToken.getExpiresAt())
                .setAccountPwdValidRemainDays(account.getPwdValidRemainDays())
                .setAuthorities(AuthorityUtils.authorityListToSet(authorities))
                .setUserProfile(account.getUserProfile())
                .setOpenId(account.getOpenId());
        globalObjectMapper.writeValue(response.getWriter(), authenticateService.getAuthResultHandler().authSucceedResult(accountInfoDTO));
        clearAuthenticationAttributes(request);
        try {
            context.getBeanIfAvailable(SlardarEventManager.class).dispatch(new LoginEvent(getAccount(), true, getHeadersAsMap(request),
                    authenticationToken));
        } catch (SlardarException e) {
            log.error(e.getLocalizedMessage());
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
