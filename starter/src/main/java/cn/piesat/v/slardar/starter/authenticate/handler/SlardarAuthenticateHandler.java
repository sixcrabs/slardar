package cn.piesat.v.slardar.starter.authenticate.handler;

import cn.piesat.v.slardar.spi.SlardarSpi;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.starter.authenticate.SlardarAuthentication;
import cn.piesat.v.slardar.starter.support.RequestWrapper;
import cn.piesat.v.slardar.starter.support.SlardarAuthenticationException;
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
