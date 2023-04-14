package cn.piesat.nj.slardar.starter.support;

import cn.piesat.nj.slardar.core.Constants;
import cn.piesat.nj.slardar.starter.SlardarUserDetails;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.util.StringUtils;

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
     * 用户名
     */
    private final String accountName;

    /**
     * openid
     */
    private final String openId;

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
     * wxapp/...
     */
    private String authType;

    /**
     * 登录设备类型 APP/PC
     */
    private LoginDeviceType loginDeviceType;


    public SlardarAuthenticationToken(String accountNameOrOpenId, String authType, SlardarUserDetails details) {
        super(Objects.isNull(details) ? null : details.getAuthorities());
        this.userDetails = details;
        if (Constants.AUTH_TYPE_WX_APP.equals(authType)) {
            this.openId = accountNameOrOpenId;
            this.accountName = null;
        } else {
            this.accountName = accountNameOrOpenId;
            this.openId = null;
        }
        this.setAuthenticated(true);
    }

    public String getAccountName() {
        return accountName;
    }

    public String getOpenId() {
        return openId;
    }

    public String getPassword() {
        return password;
    }

    public SlardarAuthenticationToken setPassword(String password) {
        this.password = password;
        return this;
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

    public LoginDeviceType getLoginDeviceType() {
        return loginDeviceType;
    }

    public SlardarAuthenticationToken setLoginDeviceType(LoginDeviceType loginDeviceType) {
        this.loginDeviceType = loginDeviceType;
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
        return this.password;
    }

    @Override
    public Object getPrincipal() {
        return StringUtils.hasText(this.accountName) ? this.accountName : this.openId;
    }
}
