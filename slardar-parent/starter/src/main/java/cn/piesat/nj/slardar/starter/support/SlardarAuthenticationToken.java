package cn.piesat.nj.slardar.starter.support;

import cn.piesat.nj.slardar.starter.SlardarUserDetails;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Objects;

/**
 * <p>
 * 传递 认证信息和 user 详细的 对象
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/26
 */
public class SlardarAuthenticationToken extends AbstractAuthenticationToken {

    private SlardarUserDetails userDetails;

    /**
     * 可以是 用户名 / openid
     */
    private final Object principal;

    /**
     * 可以是 密码
     */
    private Object credentials;

    /**
     * 当前http session id
     */
    private String sessionId;

    /**
     * 当前的租户信息如果为空 则默认是 master 租户
     */
    private String realm;

    /**
     * 认证类型
     * wxapp/...
     */
    private String authType;



    public SlardarAuthenticationToken(Object principal, SlardarUserDetails details) {
        super(Objects.isNull(details) ? null : details.getAuthorities());
        this.userDetails = details;
        this.principal = principal;
        this.setAuthenticated(true);
    }


    public String getAuthType() {
        return authType;
    }

    public SlardarAuthenticationToken setAuthType(String authType) {
        this.authType = authType;
        return this;
    }

    public SlardarUserDetails getUserDetails() {
        return userDetails;
    }

    public SlardarAuthenticationToken setUserDetails(SlardarUserDetails userDetails) {
        this.userDetails = userDetails;
        return this;
    }

    public SlardarAuthenticationToken setCredentials(Object credentials) {
        this.credentials = credentials;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public SlardarAuthenticationToken setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public String getRealm() {
        return realm;
    }

    public SlardarAuthenticationToken setRealm(String realm) {
        this.realm = realm;
        return this;
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }
}
