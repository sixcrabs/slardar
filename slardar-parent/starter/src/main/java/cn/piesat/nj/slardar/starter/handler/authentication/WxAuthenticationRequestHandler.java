package cn.piesat.nj.slardar.starter.handler.authentication;

import cn.hutool.core.util.StrUtil;
import cn.piesat.nj.slardar.starter.AuthenticationRequestHandler;
import cn.piesat.v.authx.security.infrastructure.spring.AuthxAuthenticationRequestHandler;
import cn.piesat.v.authx.security.infrastructure.spring.support.AuthxAuthentication;
import cn.piesat.v.authx.security.infrastructure.spring.support.AuthxRequestWrapper;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

/**
 * <p>
 *     TODO:
 * 处理微信 openid 登录过程
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/2/3
 */
@Component
public class WxAuthenticationRequestHandler implements AuthenticationRequestHandler {

    /**
     * 处理认证请求
     * 利用openid 去用户表里匹配用户
     *
     * @param requestWrapper
     * @return
     * @throws AuthenticationServiceException
     */
    @Override
    public AuthxAuthentication handle(AuthxRequestWrapper requestWrapper) throws AuthenticationServiceException {
        String openid = requestWrapper.getRequestParams().get("openid");
        if (StrUtil.isBlank(openid)) {
            throw new AuthenticationServiceException("需要提供openid！");
        }
        return new AuthxAuthentication(openid, null);
    }
}
