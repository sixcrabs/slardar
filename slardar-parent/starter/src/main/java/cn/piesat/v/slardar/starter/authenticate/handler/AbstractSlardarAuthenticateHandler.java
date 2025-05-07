package cn.piesat.v.slardar.starter.authenticate.handler;

import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.starter.authenticate.SlardarAuthenticatePreHandler;
import cn.piesat.v.slardar.starter.authenticate.SlardarAuthentication;
import cn.piesat.v.slardar.starter.config.SlardarProperties;
import cn.piesat.v.slardar.starter.support.RequestWrapper;
import cn.piesat.v.slardar.starter.support.SlardarAuthenticationException;
import org.springframework.security.authentication.AuthenticationServiceException;

import java.util.Collection;

import static cn.piesat.v.slardar.core.Constants.HEADER_KEY_OF_REALM;
import static cn.piesat.v.slardar.core.Constants.REALM_EMPTY;

/**
 * <p>
 * 认证 handler 抽象实现
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/23
 */
public abstract class AbstractSlardarAuthenticateHandler implements SlardarAuthenticateHandler {

    protected SlardarSpiContext context;

    /**
     * set context
     *
     * @param context
     */
    @Override
    public void initialize(SlardarSpiContext context) {
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
        // TBD: 多个 prehandler 都需要处理
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
