package cn.piesat.nj.slardar.example.impl;

import cn.piesat.nj.misc.hutool.mini.StringUtil;
import cn.piesat.v.slardar.core.Constants;
import cn.piesat.v.slardar.starter.SlardarUserDetails;
import cn.piesat.v.slardar.starter.SlardarUserDetailsServiceImpl;
import cn.piesat.v.slardar.starter.authenticate.SlardarAuthentication;
import cn.piesat.v.slardar.starter.authenticate.handler.AbstractSlardarAuthenticateHandler;
import cn.piesat.v.slardar.starter.authenticate.handler.SlardarAuthenticateHandler;
import cn.piesat.v.slardar.starter.support.RequestWrapper;
import com.google.auto.service.AutoService;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;


/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/5/23
 */
@AutoService(SlardarAuthenticateHandler.class)
public class MyOpenIdSlardarAuthenticateHandlerImpl extends AbstractSlardarAuthenticateHandler {
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
        // 判断是否正确
        authentication.setUserDetails(userDetails).setAuthenticated(true);
        return authentication;
    }

    private static final String NAME = "wx-openid";

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
     * @param requestWrapper
     * @return
     * @throws AuthenticationServiceException
     */
    @Override
    public SlardarAuthentication handleRequest(RequestWrapper requestWrapper) throws AuthenticationException {
        String openid = requestWrapper.getRequestParams().get("openid");
        if (StringUtil.isBlank(openid)) {
            throw new AuthenticationServiceException("需要提供openid！");
        }
        return new SlardarAuthentication(openid, Constants.AUTH_TYPE_WX_APP, null);
    }
}
