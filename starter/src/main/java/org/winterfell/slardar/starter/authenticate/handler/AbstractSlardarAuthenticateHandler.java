package org.winterfell.slardar.starter.authenticate.handler;

import org.winterfell.slardar.core.SlardarException;
import org.winterfell.slardar.core.SlardarContext;
import org.winterfell.slardar.starter.authenticate.SlardarAuthenticatePreHandler;
import org.winterfell.slardar.starter.authenticate.SlardarAuthentication;
import org.winterfell.slardar.starter.SlardarProperties;
import org.winterfell.slardar.starter.support.RequestWrapper;
import org.winterfell.slardar.starter.support.SlardarAuthenticationException;
import org.springframework.security.authentication.AuthenticationServiceException;

import java.util.Collection;

import static org.winterfell.slardar.core.Constants.HEADER_KEY_OF_REALM;
import static org.winterfell.slardar.core.Constants.REALM_EMPTY;

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
     * set context
     *
     * @param context
     */
    @Override
    public void initialize(SlardarContext context) {
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
    public SlardarAuthentication doAuthenticate(SlardarAuthentication authentication) throws SlardarAuthenticationException {
        // TBD: 多个 pre handler 都需要处理
        Collection<SlardarAuthenticatePreHandler> preHandlers = context.getBeans(SlardarAuthenticatePreHandler.class);
        if (preHandlers != null && !preHandlers.isEmpty()) {
            for (SlardarAuthenticatePreHandler preHandler : preHandlers) {
                try {
                    preHandler.preHandle(authentication);
                } catch (SlardarException e) {
                    throw new AuthenticationServiceException(e.getLocalizedMessage());
                }
            }
        }
        return doAuthenticate0(authentication);
    }

    protected SlardarProperties getProperties() {
        return context.getBeanIfAvailable(SlardarProperties.class);
    }

    protected String getRealm(final RequestWrapper requestWrapper) {
        if (requestWrapper.getRequestHeaders().containsKey(HEADER_KEY_OF_REALM)) {
            return requestWrapper.getRequestHeaders().getOrDefault(HEADER_KEY_OF_REALM, REALM_EMPTY);
        } else {
            return requestWrapper.getRequestParams().getOrDefault("realm", REALM_EMPTY);
        }
    }


    /**
     * 子类实现
     *
     * @param authentication
     * @return
     */
    protected abstract SlardarAuthentication doAuthenticate0(SlardarAuthentication authentication);
}
