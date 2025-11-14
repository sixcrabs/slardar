package org.winterfell.slardar.starter.filter;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.winterfell.misc.hutool.mini.StringUtil;
import org.winterfell.slardar.core.SlardarException;
import org.winterfell.slardar.core.SlardarSecurityHelper;
import org.winterfell.slardar.core.domain.Account;
import org.winterfell.slardar.core.SlardarContext;
import org.winterfell.slardar.spi.SlardarSpiFactory;
import org.winterfell.slardar.spi.crypto.SlardarCrypto;
import org.winterfell.slardar.starter.SlardarEventManager;
import org.winterfell.slardar.starter.authenticate.SlardarAuthenticateService;
import org.winterfell.slardar.starter.authenticate.SlardarUserDetails;
import org.winterfell.slardar.starter.authenticate.SlardarAuthentication;
import org.winterfell.slardar.starter.SlardarProperties;
import org.winterfell.slardar.starter.provider.AccountProvider;
import org.winterfell.slardar.starter.support.LoginDeviceType;
import org.winterfell.slardar.starter.support.SecUtil;
import org.winterfell.slardar.starter.support.SlardarAuthenticationException;
import org.winterfell.slardar.starter.support.event.LogoutEvent;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.winterfell.slardar.core.Constants.*;
import static org.winterfell.slardar.starter.support.HttpServletUtil.*;

/**
 * <p>
 * 处理登录后的请求
 * <strong>前提是必须登录</strong>
 * <ul>
 *     <li>/profile 用户详细信息</li>
 *     <li>/logout   登出</li>
 *     <li>/changePwd   修改密码</li>
 * </ul>
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/13
 */
public class SlardarAuthenticatedRequestFilter extends GenericFilterBean {

    private final List<RequestMatcher> requestMatchers;

    private final SlardarContext context;

    @Autowired
    private SlardarAuthenticateService authenticateService;

    @Autowired
    private UserDetailsService userDetailsService;

    private final SlardarProperties properties;

    /* ====== 密码校验规则 ====== */
    private static final Pattern LENGTH = Pattern.compile("^.{8,32}$");
    private static final Pattern UPPER = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPECIAL_PT = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?/`]");
    private static final Pattern NO_SPACE = Pattern.compile("^\\S+$");

    public SlardarAuthenticatedRequestFilter(SlardarProperties properties, SlardarContext context) {
        this.context = context;
        this.requestMatchers = Lists.newArrayList(
                new AntPathRequestMatcher(AUTH_PROFILE_FETCH_URL, HttpMethod.POST.name()),
                new AntPathRequestMatcher(AUTH_LOGOUT_URL, HttpMethod.POST.name()),
                new AntPathRequestMatcher(USER_CHANGE_PWD_URL, HttpMethod.POST.name()));
        this.properties = properties;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (!requestMatches(request)) {
            chain.doFilter(request, response);
        } else {
            String uri = request.getRequestURI();
            switch (uri) {
                case AUTH_PROFILE_FETCH_URL:
                    handleUserDetails(request, response);
                    break;
                case AUTH_LOGOUT_URL:
                    handleLogout(request, response);
                    break;
                case USER_CHANGE_PWD_URL:
                    handleChangePwd(request, response);
                    break;
            }
        }
    }

    /**
     * 处理修改密码请求
     * - 支持加密后的密码
     *
     * @param request
     * @param response
     */
    private void handleChangePwd(HttpServletRequest request, HttpServletResponse response) {
        Account account = SecUtil.getAccount();
        String accountName = account.getName();
        String password = account.getPassword();
        String oldPwd = request.getParameter("oldPwd");
        String newPwd = request.getParameter("newPwd");
        PasswordEncoder passwordEncoder = context.getBeanOrDefault(PasswordEncoder.class, new BCryptPasswordEncoder());
        AccountProvider accountProvider = context.getBeanIfAvailable(AccountProvider.class);
        String tokenValue = authenticateService.getTokenValueFromServlet(request);
        if (properties.getLogin().getEncrypt().isEnabled()) {
            // 当登录启用密码加密时，修改密码同样需要对旧、新密码进行加密
            SlardarSpiFactory spiFactory = context.getBeanIfAvailable(SlardarSpiFactory.class);
            SlardarCrypto crypto = spiFactory.findCrypto(properties.getLogin().getEncrypt().getMode());
            try {
                oldPwd = crypto.decrypt(oldPwd);
                newPwd = crypto.decrypt(newPwd);
            } catch (SlardarException e) {
                sendError(request, response, StringUtil.format("解密[{}]失败:{}", properties.getLogin().getEncrypt().getMode(),
                        e.getLocalizedMessage()));
                return;
            }
        }
        // 比较oldPwd 是否一致
        if (!passwordEncoder.matches(oldPwd, password)) {
            sendError(request, response, HttpStatus.INTERNAL_SERVER_ERROR, "Old password is not correct");
            return;
        }
        try {
            // 校验新密码是否符合指定规则
            if (validatePwd(newPwd)) {
                boolean b = accountProvider.setPwd(accountName, newPwd, account.getRealm());
                if (b) {
                    // 登出当前账号
                    boolean succeed = authenticateService.withdrawToken(tokenValue);
                    if (succeed) {
                        SlardarAuthentication logoutAuth = new SlardarAuthentication(accountName, null);
                        logoutAuth.setAuthenticated(false);
                        SecurityContextHolder.getContext().setAuthentication(logoutAuth);
                        SlardarSecurityHelper.getContext().setAuthenticated(false);
                    }
                    sendJsonOk(response, makeSuccessResult(Collections.emptyMap()));
                } else {
                    sendError(request, response, HttpStatus.INTERNAL_SERVER_ERROR, "Password change failed");
                }
            }
        } catch (SlardarException e) {
            sendError(request, response, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 校验密码是否符合指定规则
     *
     * @param pwdStr 明文密码
     * @return
     * @throws SlardarException
     */
    private boolean validatePwd(String pwdStr) throws SlardarException {
        if (StringUtil.isBlank(pwdStr)) {
            throw new SlardarException("Password cannot be empty");
        }
        if (!LENGTH.matcher(pwdStr).matches()) {
            throw new SlardarException("Password length must be between 8 and 32 characters");
        }
        if (!UPPER.matcher(pwdStr).find()) {
            throw new SlardarException("Password must contain at least one uppercase letter");
        }
        if (!DIGIT.matcher(pwdStr).find()) {
            throw new SlardarException("Password must contain at least one digit");
        }
        if (!SPECIAL_PT.matcher(pwdStr).find()) {
            throw new SlardarException("Password must contain at least one special character");
        }
        if (!NO_SPACE.matcher(pwdStr).matches()) {
            throw new SlardarException("Password must not contain any spaces");
        }
        return true;
    }

    /**
     * 处理获取用户详情请求
     *
     * @param request
     * @param response
     * @throws IOException
     */
    private void handleUserDetails(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Account account = SecUtil.getAccount();
        Account cloned = new Account();
        if (account != null) {
            cloned = account.clone();
            cloned.setPassword("");
        }
        sendJsonOk(response, makeSuccessResult(new SlardarUserDetails(cloned)));
    }


    /**
     * 处理登出请求
     *
     * @param request
     * @param response
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) {
        String tokenValue = authenticateService.getTokenValueFromServlet(request);
        if (StringUtil.isBlank(tokenValue)) {
            sendJson(response, makeErrorResult("Not logged in", HttpStatus.UNAUTHORIZED.value()), HttpStatus.UNAUTHORIZED, request.getHeader("Origin"));
        }
        LoginDeviceType deviceType = getDeviceType(request);
        if (authenticateService.isExpired(tokenValue, deviceType)) {
            sendJson(response, makeErrorResult("Token has been expired", HttpStatus.UNAUTHORIZED.value()), HttpStatus.UNAUTHORIZED, request.getHeader("Origin"));
        }
        String username = authenticateService.getUsernameFromTokenValue(tokenValue);
        SlardarUserDetails userDetails = (SlardarUserDetails) userDetailsService.loadUserByUsername(username);
        Account account = userDetails.getAccount();
        boolean b = authenticateService.withdrawToken(tokenValue, deviceType);
        if (b) {
            SlardarAuthentication logoutAuth = new SlardarAuthentication(username, null);
            logoutAuth.setAuthenticated(false);
            SecurityContextHolder.getContext().setAuthentication(logoutAuth);
            SlardarSecurityHelper.getContext().setAuthenticated(false);
            sendJsonOk(response, makeSuccessResult("Logout Successful"));
            try {
                context.getBeanIfAvailable(SlardarEventManager.class).dispatch(new LogoutEvent(account, request));
            } catch (SlardarException e) {
                throw new RuntimeException(e);
            }
        } else {
            sendJson(response, makeErrorResult("Server error...", HttpStatus.EXPECTATION_FAILED.value()), HttpStatus.EXPECTATION_FAILED, request.getHeader("Origin"));
        }
    }

    private boolean requestMatches(HttpServletRequest request) {
        return this.requestMatchers.stream().anyMatch(requestMatcher -> requestMatcher.matches(request));
    }
}