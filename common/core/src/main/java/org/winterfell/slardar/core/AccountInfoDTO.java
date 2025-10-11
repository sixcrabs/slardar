package org.winterfell.slardar.core;

import org.winterfell.slardar.core.entity.UserProfile;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * <p>
 * 账户信息DTO 用于登录成功后返回 以及 /userdetails 返回
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/16
 */
public class AccountInfoDTO implements Serializable {

    private Boolean accountExpired;

    private Boolean accountLocked;

    private String accountName;

    private UserProfile userProfile;

    private String openId;

    private String token;

    /**
     * token 过期时间
     */
    private LocalDateTime tokenExpiresAt;

    /**
     * authority
     */
    private Set<String> authorities;

    /**
     * 账户口令剩余有效天数
     * 若应用方返回该参数，则会进行相应判断 置空则不会判断
     */
    private Integer accountPwdValidRemainDays;


    public LocalDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public AccountInfoDTO setTokenExpiresAt(LocalDateTime tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
        return this;
    }

    public Integer getAccountPwdValidRemainDays() {
        return accountPwdValidRemainDays;
    }

    public AccountInfoDTO setAccountPwdValidRemainDays(Integer accountPwdValidRemainDays) {
        this.accountPwdValidRemainDays = accountPwdValidRemainDays;
        return this;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public AccountInfoDTO setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
        return this;
    }

    @Override
    public String toString() {
        return "AccountInfoDTO{" +
                "accountExpired=" + accountExpired +
                ", accountLocked=" + accountLocked +
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

    public Boolean getAccountExpired() {
        return accountExpired;
    }

    public AccountInfoDTO setAccountExpired(Boolean accountExpired) {
        this.accountExpired = accountExpired;
        return this;
    }

    public Boolean getAccountLocked() {
        return accountLocked;
    }

    public AccountInfoDTO setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
        return this;
    }
}
