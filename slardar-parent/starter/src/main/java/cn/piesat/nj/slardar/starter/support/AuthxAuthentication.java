package cn.piesat.nj.slardar.starter.support;

import cn.piesat.v.authx.security.infrastructure.spring.userdetails.AuthxUserDetails;
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
public class AuthxAuthentication extends AbstractAuthenticationToken {

    private AuthxUserDetails userDetails;

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
     * 当前的租户id 如果为空 则默认是 master 租户
     */
    private String realm;

    /**
     * 认证类型
     * wxapp/...
     */
    private String authType;



    public AuthxAuthentication(Object principal, AuthxUserDetails details) {
        super(Objects.isNull(details) ? null : details.getAuthorities());
        this.userDetails = details;
        this.principal = principal;
        this.setAuthenticated(true);
    }


    public String getAuthType() {
        return authType;
    }

    public AuthxAuthentication setAuthType(String authType) {
        this.authType = authType;
        return this;
    }

    public AuthxUserDetails getUserDetails() {
        return userDetails;
    }

    public AuthxAuthentication setUserDetails(AuthxUserDetails userDetails) {
        this.userDetails = userDetails;
        return this;
    }

    public AuthxAuthentication setCredentials(Object credentials) {
        this.credentials = credentials;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public AuthxAuthentication setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public String getRealm() {
        return realm;
    }

    public AuthxAuthentication setRealm(String realm) {
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
