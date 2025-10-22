package org.winterfell.slardar.starter.authenticate;

import org.winterfell.slardar.starter.support.LoginDeviceType;
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
public class SlardarAuthentication extends AbstractAuthenticationToken {

    private SlardarUserDetails userDetails;

    /**
     * 用户名
     */
    private final String accountName;

//    private final String openId;

    /**
     * 密码
     */
    private String password;

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
     * open-id/ldap/....
     */
    private String authType;

    /**
     * 登录设备类型 APP/PC
     */
    private LoginDeviceType loginDeviceType;

    /**
     * 请求客户端 IP
     */
    private String reqClientIp;

    /**
     * @param principal 用户名
     * @param authType  basic: BasicAuth
     * @param details
     */
    public SlardarAuthentication(String principal, String authType, SlardarUserDetails details) {
        super(Objects.isNull(details) ? null : details.getAuthorities());
        this.userDetails = details;
        this.accountName = principal;
        this.authType = authType;
    }

    public SlardarAuthentication(SlardarUserDetails details) {
        super(Objects.isNull(details) ? null : details.getAuthorities());
        this.userDetails = details;
        this.accountName = details.getUsername();
    }

    public String getReqClientIp() {
        return reqClientIp;
    }

    public SlardarAuthentication setReqClientIp(String reqClientIp) {
        this.reqClientIp = reqClientIp;
        return this;
    }

    public String getAccountName() {
        return accountName;
    }


    public String getPassword() {
        return password;
    }

    public SlardarAuthentication setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getAuthType() {
        return authType;
    }

    public SlardarAuthentication setAuthType(String authType) {
        this.authType = authType;
        return this;
    }

    public SlardarUserDetails getUserDetails() {
        return userDetails;
    }

    public SlardarAuthentication setUserDetails(SlardarUserDetails userDetails) {
        this.userDetails = userDetails;
        return this;
    }

    public LoginDeviceType getLoginDeviceType() {
        return loginDeviceType;
    }

    public SlardarAuthentication setLoginDeviceType(LoginDeviceType loginDeviceType) {
        this.loginDeviceType = loginDeviceType;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public SlardarAuthentication setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public String getRealm() {
        return realm;
    }

    public SlardarAuthentication setRealm(String realm) {
        this.realm = realm;
        return this;
    }

    /**
     * 账户密码等凭证
     *
     * @return
     */
    @Override
    public Object getCredentials() {
        return this.password;
    }

    /**
     * 账户名 或 openid 等
     * TODO: principal 需要封装
     *
     * @return
     */
    @Override
    public Object getPrincipal() {
        return this.accountName;
//        return StringUtils.hasText(this.accountName) ? this.accountName : this.openId;
    }
}
