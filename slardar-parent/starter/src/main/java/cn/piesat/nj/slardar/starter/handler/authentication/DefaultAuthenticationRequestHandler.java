package cn.piesat.nj.slardar.starter.handler.authentication;

import cn.piesat.nj.slardar.starter.AuthenticationRequestHandler;
import cn.piesat.nj.slardar.starter.config.SlardarProperties;
import cn.piesat.nj.slardar.starter.filter.SlardarLoginProcessingFilter;
import cn.piesat.nj.slardar.starter.support.SlardarAuthenticationToken;
import cn.piesat.nj.slardar.starter.support.captcha.CaptchaComponent;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
                throw new AuthenticationServiceException("需要提供验证码");
            }
            boolean b = captchaComponent.verify(requestWrapper.getSessionId(), code);
            if (!b) {
                throw new AuthenticationServiceException("验证码无效");
            }
        }
        String username = requestWrapper.getRequestParams().get("username");
        String password = requestWrapper.getRequestParams().get("password");
        // TODO: 租户id
        String realm = requestWrapper.getRequestParams().getOrDefault("realm", "");
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new AuthenticationServiceException("`username` and `password` should not be null");
        }
        return new SlardarAuthenticationToken(username, null)
                .setRealm(realm)
                .setSessionId(requestWrapper.getSessionId())
                .setCredentials(password);
    }
}
