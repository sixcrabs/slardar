package cn.piesat.nj.slardar.starter.authenticate.handler;

import cn.piesat.nj.slardar.spi.SlardarSpiContext;
import cn.piesat.nj.slardar.starter.authenticate.SlardarAuthentication;
import cn.piesat.nj.slardar.starter.support.RequestWrapper;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;

/**
 * <p>
 * 认证 handler 接口
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/23
 */
public interface SlardarAuthenticateHandler {

    /**
     * 注入 context
     * @param context
     */
    void setSlardarContext(SlardarSpiContext context);

    /**
     * 认证处理类型 用于区分
     * @return
     */
    String type();

    /**
     * 处理认证请求
     * @param requestWrapper
     * @return
     * @throws AuthenticationServiceException
     */
    SlardarAuthentication handleRequest(RequestWrapper requestWrapper) throws AuthenticationException;

    /**
     * 执行认证
     * @param authentication
     * @return
     * @throws AuthenticationServiceException
     */
    SlardarAuthentication doAuthenticate(SlardarAuthentication authentication) throws AuthenticationException;
}
