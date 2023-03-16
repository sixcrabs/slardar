package cn.piesat.nj.slardar.core;

import cn.piesat.nj.slardar.core.entity.UserProfile;

import java.io.Serializable;

/**
 * <p>
 * 账户信息DTO 用于登录成功后返回 以及 /userdetails 返回
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/16
 */
public class AccountInfoDTO implements Serializable {

    private boolean accountNonExpired;

    private boolean accountNonLocked;

    private String accountName;

    private UserProfile userProfile;

    private String openId;

    private String token;


    @Override
    public String toString() {
        return "AccountInfoDTO{" +
                "accountNonExpired=" + accountNonExpired +
                ", accountNonLocked=" + accountNonLocked +
                ", accountName='" + accountName + '\'' +
                ", userProfile=" + userProfile +
                ", openId='" + openId + '\'' +
                ", token='" + token + '\'' +
                '}';
    }

    public String getToken() {
        return token;
    }

    public AccountInfoDTO setToken(String token) {
        this.token = token;
        return this;
    }

    public String getAccountName() {
        return accountName;
    }

    public AccountInfoDTO setAccountName(String accountName) {
        this.accountName = accountName;
        return this;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public AccountInfoDTO setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        return this;
    }

    public String getOpenId() {
        return openId;
    }

    public AccountInfoDTO setOpenId(String openId) {
        this.openId = openId;
        return this;
    }

    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    public AccountInfoDTO setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
        return this;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public AccountInfoDTO setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
        return this;
    }
}
