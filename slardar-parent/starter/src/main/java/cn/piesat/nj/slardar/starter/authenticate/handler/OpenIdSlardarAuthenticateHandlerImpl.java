package cn.piesat.nj.slardar.starter.authenticate.handler;

import cn.hutool.core.util.StrUtil;
import cn.piesat.nj.slardar.core.Constants;
import cn.piesat.nj.slardar.starter.authenticate.SlardarAuthentication;
import cn.piesat.nj.slardar.starter.filter.SlardarLoginProcessingFilter;
import com.google.auto.service.AutoService;
import org.springframework.security.authentication.AuthenticationServiceException;

/**
 * <p>
 * 微信等 openid 方式认证
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/23
 */
@AutoService(SlardarAuthenticateHandler.class)
public class OpenIdSlardarAuthenticateHandlerImpl extends AbstractSlardarAuthenticateHandler {

    private static final String NAME = "open-id";

    /**
     * 认证处理类型 用于区分
     *
     * @return
     */
    @Override
    public String type() {
        return NAME;
    }


    @Override
    public SlardarAuthentication handleRequest(SlardarLoginProcessingFilter.RequestWrapper requestWrapper) throws AuthenticationServiceException {
        String openid = requestWrapper.getRequestParams().get("openid");
        if (StrUtil.isBlank(openid)) {
            throw new AuthenticationServiceException("需要提供openid！");
        }
        return new SlardarAuthentication(openid, Constants.AUTH_TYPE_WX_APP, null);
    }

    /**
     * 子类实现
     *
     * @param authentication
     * @return
     */
    @Override
    SlardarAuthentication doAuthenticate0(SlardarAuthentication authentication) {
        return null;
    }
}
