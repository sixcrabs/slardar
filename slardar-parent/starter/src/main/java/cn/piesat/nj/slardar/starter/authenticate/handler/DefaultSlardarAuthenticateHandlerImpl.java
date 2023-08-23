package cn.piesat.nj.slardar.starter.authenticate.handler;

import cn.piesat.nj.slardar.core.Constants;
import cn.piesat.nj.slardar.starter.SlardarUserDetails;
import cn.piesat.nj.slardar.starter.SlardarUserDetailsServiceImpl;
import cn.piesat.nj.slardar.starter.authenticate.SlardarAuthentication;
import cn.piesat.nj.slardar.starter.config.SlardarProperties;
import cn.piesat.nj.slardar.starter.filter.SlardarLoginProcessingFilter;
import cn.piesat.nj.slardar.starter.support.captcha.CaptchaComponent;
import com.google.auto.service.AutoService;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.Objects;

import static cn.piesat.nj.slardar.core.Constants.HEADER_KEY_OF_REALM;
import static cn.piesat.nj.slardar.core.Constants.REALM_MASTER;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/23
 */
@AutoService(SlardarAuthenticateHandler.class)
public class DefaultSlardarAuthenticateHandlerImpl extends AbstractSlardarAuthenticateHandler {

    public static final String NAME = "default";

    /**
     * 认证处理类型 用于区分
     *
     * @return
     */
    @Override
    public String type() {
        return NAME;
    }

    /**
     * 处理认证请求
     *
     * @return
     * @throws AuthenticationServiceException
     */
    @Override
    public SlardarAuthentication handleRequest(SlardarLoginProcessingFilter.RequestWrapper requestWrapper) throws AuthenticationServiceException {
        // 根据设置来确定是否需要启用验证码流程
        SlardarProperties properties = getProperties();
        CaptchaComponent captchaComponent = context.getBeanIfAvailable(CaptchaComponent.class);
        if (properties.getLogin().getCaptchaEnabled()) {
            String code = requestWrapper.getRequestParams().get("authCode");
            if (!StringUtils.hasLength(code)) {
                throw new AuthenticationServiceException("需要提供验证码[authCode]");
            }
            if (!captchaComponent.verify(requestWrapper.getSessionId(), code)) {
                throw new AuthenticationServiceException("验证码[authCode]无效");
            }
        }
        String username = requestWrapper.getRequestParams().get("username");
        String password = requestWrapper.getRequestParams().get("password");
        // 租户信息 默认为 master
        String realm = getRealm(requestWrapper);
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new AuthenticationServiceException("`username` and `password` should not be null");
        }
        return new SlardarAuthentication(username, Constants.AUTH_TYPE_NORMAL, null)
                .setRealm(realm)
                .setLoginDeviceType(requestWrapper.getLoginDeviceType())
                .setSessionId(requestWrapper.getSessionId())
                .setPassword(password);
    }



    private String getRealm(final SlardarLoginProcessingFilter.RequestWrapper requestWrapper) {
        if (requestWrapper.getRequestHeaders().containsKey(HEADER_KEY_OF_REALM)) {
            return requestWrapper.getRequestHeaders().getOrDefault(HEADER_KEY_OF_REALM, REALM_MASTER);
        } else {
            return requestWrapper.getRequestParams().getOrDefault("realm", REALM_MASTER);
        }
    }

    /**
     * 子类实现
     *
     * @param authentication
     * @return
     */
    @Override
    protected SlardarAuthentication doAuthenticate0(SlardarAuthentication authentication) {
        SlardarUserDetailsServiceImpl userDetailsService = (SlardarUserDetailsServiceImpl) context.getBeanIfAvailable(UserDetailsService.class);
        PasswordEncoder passwordEncoder = context.getPwdEncoder();
        // 用户密码方式认证
        String accountName = authentication.getAccountName();
        UserDetails userDetails = userDetailsService.loadUserByAccount(accountName, authentication.getRealm());
        // 验证密码是否正确
        if (!Objects.isNull(userDetails)) {
            if (passwordEncoder.matches(authentication.getPassword(), userDetails.getPassword())) {
                try {
                    authentication.setUserDetails((SlardarUserDetails) userDetails);
                    return authentication;
                } catch (Exception e) {
                    throw new AuthenticationServiceException(e.getLocalizedMessage());
                }
            } else {
                throw new AuthenticationServiceException(String.format("账户 [%s] 密码不匹配", userDetails.getUsername()));
            }
        } else {
            throw new UsernameNotFoundException("account not found");
        }
    }
}
