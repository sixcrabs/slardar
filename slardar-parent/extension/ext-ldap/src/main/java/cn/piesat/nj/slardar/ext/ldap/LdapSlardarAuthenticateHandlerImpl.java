package cn.piesat.nj.slardar.ext.ldap;

import cn.piesat.nj.slardar.starter.authenticate.SlardarAuthentication;
import cn.piesat.nj.slardar.starter.authenticate.handler.AbstractSlardarAuthenticateHandler;
import cn.piesat.nj.slardar.starter.authenticate.handler.SlardarAuthenticateHandler;
import cn.piesat.nj.slardar.starter.filter.SlardarLoginProcessingFilter;
import com.google.auto.service.AutoService;
import org.springframework.security.authentication.AuthenticationServiceException;

/**
 * <p>
 * .TODO:
 * 使用 LDAP 进行身份认证
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/23
 */
@AutoService(SlardarAuthenticateHandler.class)
public class LdapSlardarAuthenticateHandlerImpl extends AbstractSlardarAuthenticateHandler {

    private static final String NAME = "LDAP";

    /**
     * 认证处理类型 用于区分
     *
     * @return
     */
    @Override
    public String type() {
        return NAME;
    }

    /**
     * 处理认证请求
     *
     * @param requestWrapper
     * @return
     * @throws AuthenticationServiceException
     */
    @Override
    public SlardarAuthentication handleRequest(SlardarLoginProcessingFilter.RequestWrapper requestWrapper) throws AuthenticationServiceException {
        return null;
    }


    /**
     * 子类实现
     * 访问ldap 进行认证 和 用户身份同步
     *
     * @param authentication
     * @return
     */
    @Override
    protected SlardarAuthentication doAuthenticate0(SlardarAuthentication authentication) {
        return null;
    }
}
