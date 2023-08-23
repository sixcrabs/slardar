package cn.piesat.nj.slardar.starter.authenticate.handler;

import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.authenticate.SlardarAuthenticatePreHandler;
import cn.piesat.nj.slardar.starter.authenticate.SlardarAuthentication;
import cn.piesat.nj.slardar.starter.config.SlardarProperties;
import org.springframework.security.authentication.AuthenticationServiceException;

/**
 * <p>
 * 认证 handler 抽象实现
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/23
 */
public abstract class AbstractSlardarAuthenticateHandler implements SlardarAuthenticateHandler {

    protected SlardarContext context;

    /**
     * 注入 context
     *
     * @param context
     */
    @Override
    public void setSlardarContext(SlardarContext context) {
        this.context = context;
    }

    /**
     * 执行认证
     * - pre
     * - post
     *
     * @param authentication@return
     * @throws AuthenticationServiceException
     */
    @Override
    public SlardarAuthentication doAuthenticate(SlardarAuthentication authentication) throws AuthenticationServiceException {
        SlardarAuthenticatePreHandler authenticationBeforeHandler = context.getBeanIfAvailable(SlardarAuthenticatePreHandler.class);
        if (authenticationBeforeHandler != null) {
            try {
                authenticationBeforeHandler.preHandle(authentication);
            } catch (SlardarException e) {
                throw new AuthenticationServiceException(e.getLocalizedMessage());
            }
        }
        return doAuthenticate0(authentication);
    }

    protected SlardarProperties getProperties() {
        return context.getBeanIfAvailable(SlardarProperties.class);
    }


    /**
     * 子类实现
     * @param authentication
     * @return
     */
    abstract SlardarAuthentication doAuthenticate0(SlardarAuthentication authentication);
}
