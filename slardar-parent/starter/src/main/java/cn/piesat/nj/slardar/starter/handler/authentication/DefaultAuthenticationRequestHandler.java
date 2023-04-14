package cn.piesat.nj.slardar.starter.handler.authentication;

import cn.piesat.nj.slardar.core.Constants;
import cn.piesat.nj.slardar.starter.AuthenticationRequestHandler;
import cn.piesat.nj.slardar.starter.config.SlardarProperties;
import cn.piesat.nj.slardar.starter.filter.SlardarLoginProcessingFilter;
import cn.piesat.nj.slardar.starter.support.SlardarAuthenticationToken;
import cn.piesat.nj.slardar.starter.support.captcha.CaptchaComponent;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static cn.piesat.nj.slardar.core.Constants.HEADER_KEY_OF_REALM;
import static cn.piesat.nj.slardar.core.Constants.REALM_MASTER;

/**
 * <p>
 * 处理带有验证码的认证请求
 * 判断验证码是否有效，有效则 调用 authProvider
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/26
 */
@Component
public class DefaultAuthenticationRequestHandler implements AuthenticationRequestHandler {

    private final SlardarProperties properties;

    private final CaptchaComponent captchaComponent;

    public DefaultAuthenticationRequestHandler(SlardarProperties properties, CaptchaComponent captchaComponent) {
        this.properties = properties;
        this.captchaComponent = captchaComponent;
    }

    @Override
    public SlardarAuthenticationToken handle(SlardarLoginProcessingFilter.RequestWrapper requestWrapper) throws AuthenticationServiceException {
        // 根据设置来确定是否需要启用验证码流程
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
        return new SlardarAuthenticationToken(username, Constants.AUTH_TYPE_NORMAL, null)
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
}
