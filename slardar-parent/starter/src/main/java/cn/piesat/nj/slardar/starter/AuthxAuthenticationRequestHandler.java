package cn.piesat.nj.slardar.starter;

import cn.piesat.v.authx.security.infrastructure.spring.handler.authentication.AuthxDefaultAuthenticationRequestHandler;
import cn.piesat.v.authx.security.infrastructure.spring.support.AuthxAuthentication;
import cn.piesat.v.authx.security.infrastructure.spring.support.AuthxRequestWrapper;
import org.springframework.security.authentication.AuthenticationServiceException;

/**
 * <p>
 * 处理认证请求
 * 由实际场景去实现不同处理
 * @see AuthxDefaultAuthenticationRequestHandler
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/26
 */
public interface AuthxAuthenticationRequestHandler {


    /**
     * 处理认证请求
     * @param requestWrapper
     * @return
     * @throws AuthenticationServiceException
     */
    AuthxAuthentication handle(AuthxRequestWrapper requestWrapper) throws AuthenticationServiceException;


}
