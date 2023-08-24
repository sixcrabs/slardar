package cn.piesat.nj.slardar.starter.authenticate.handler;

import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.authenticate.SlardarAuthenticatePreHandler;
import cn.piesat.nj.slardar.starter.authenticate.SlardarAuthentication;
import cn.piesat.nj.slardar.starter.config.SlardarProperties;
import cn.piesat.nj.slardar.starter.filter.SlardarLoginProcessingFilter;
import org.springframework.security.authentication.AuthenticationServiceException;

import static cn.piesat.nj.slardar.core.Constants.HEADER_KEY_OF_REALM;
import static cn.piesat.nj.slardar.core.Constants.REALM_MASTER;

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

    protected String getRealm(final SlardarLoginProcessingFilter.RequestWrapper requestWrapper) {
        if (requestWrapper.getRequestHeaders().containsKey(HEADER_KEY_OF_REALM)) {
            return requestWrapper.getRequestHeaders().getOrDefault(HEADER_KEY_OF_REALM, REALM_MASTER);
        } else {
            return requestWrapper.getRequestParams().getOrDefault("realm", REALM_MASTER);
        }
    }


    /**
     * 子类实现
     * @param authentication
     * @return
     */
    protected abstract SlardarAuthentication doAuthenticate0(SlardarAuthentication authentication);
}
