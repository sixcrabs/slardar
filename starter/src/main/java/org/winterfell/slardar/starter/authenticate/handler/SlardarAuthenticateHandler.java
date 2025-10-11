package org.winterfell.slardar.starter.authenticate.handler;

import org.winterfell.slardar.spi.SlardarSpi;
import org.winterfell.slardar.starter.authenticate.SlardarAuthentication;
import org.winterfell.slardar.starter.support.RequestWrapper;
import org.winterfell.slardar.starter.support.SlardarAuthenticationException;
import org.springframework.security.authentication.AuthenticationServiceException;

/**
 * <p>
 * 认证 handler 接口
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/23
 */
public interface SlardarAuthenticateHandler extends SlardarSpi {

    /**
     * 处理认证请求
     * @param requestWrapper
     * @return
     * @throws AuthenticationServiceException
     */
    SlardarAuthentication handleRequest(RequestWrapper requestWrapper) throws SlardarAuthenticationException;

    /**
     * 执行认证
     * @param authentication
     * @return
     * @throws AuthenticationServiceException
     */
    SlardarAuthentication doAuthenticate(SlardarAuthentication authentication) throws SlardarAuthenticationException;
}
