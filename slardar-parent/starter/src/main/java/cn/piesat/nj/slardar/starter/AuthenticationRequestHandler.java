package cn.piesat.nj.slardar.starter;

import cn.piesat.nj.slardar.starter.filter.SlardarLoginProcessingFilter;
import cn.piesat.nj.slardar.starter.support.SlardarAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;

/**
 * <p>
 * 处理认证请求
 * 由实际场景去实现不同处理
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/26
 */
public interface AuthenticationRequestHandler {


    /**
     * 处理认证请求
     * @param requestWrapper
     * @return
     * @throws AuthenticationServiceException
     */
    SlardarAuthenticationToken handle(SlardarLoginProcessingFilter.RequestWrapper requestWrapper) throws AuthenticationServiceException;


}
