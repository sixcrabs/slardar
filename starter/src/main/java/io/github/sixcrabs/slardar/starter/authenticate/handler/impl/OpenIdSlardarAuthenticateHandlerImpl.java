package io.github.sixcrabs.slardar.starter.authenticate.handler.impl;

import io.github.sixcrabs.slardar.core.Constants;
import io.github.sixcrabs.slardar.starter.authenticate.SlardarUserDetails;
import io.github.sixcrabs.slardar.starter.authenticate.SlardarUserDetailsServiceImpl;
import io.github.sixcrabs.slardar.starter.authenticate.SlardarAuthentication;
import io.github.sixcrabs.slardar.starter.authenticate.handler.AbstractSlardarAuthenticateHandler;
import io.github.sixcrabs.slardar.starter.authenticate.handler.SlardarAuthenticateHandler;
import io.github.sixcrabs.slardar.starter.support.RequestWrapper;
import io.github.sixcrabs.slardar.starter.support.SlardarAuthenticationException;
import com.google.auto.service.AutoService;
import io.github.sixcrabs.winterfell.mini.StringUtil;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * <p>
 * 微信等 openid 方式认证
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/23
 */
@AutoService(SlardarAuthenticateHandler.class)
public class OpenIdSlardarAuthenticateHandlerImpl extends AbstractSlardarAuthenticateHandler {

    private static final String NAME = "open-id";

    private String phone;

    /**
     * 认证处理类型 用于区分
     *
     * @return
     */
    @Override
    public String name() {
        return NAME;
    }


    @Override
    public SlardarAuthentication handleRequest(RequestWrapper requestWrapper) throws SlardarAuthenticationException {
        String openid = requestWrapper.getRequestParams().get("openid");
        if (StringUtil.isBlank(openid)) {
            throw new AuthenticationServiceException("需要提供openid！");
        }
        return new SlardarAuthentication(openid, Constants.AUTH_TYPE_WX_APP, null);
    }

    /**
     * 子类实现
     *
     * @param authentication
     * @return
     */
    @Override
    protected SlardarAuthentication doAuthenticate0(SlardarAuthentication authentication) {
        String openId = authentication.getAccountName();
        //
        SlardarUserDetailsServiceImpl detailsService = (SlardarUserDetailsServiceImpl) context.getBeanIfAvailable(UserDetailsService.class);
        SlardarUserDetails userDetails = (SlardarUserDetails) detailsService.loadUserByOpenId(openId);
        // TODO: 判断是否正确
        authentication.setUserDetails(userDetails).setAuthenticated(true);
        return authentication;
    }
}