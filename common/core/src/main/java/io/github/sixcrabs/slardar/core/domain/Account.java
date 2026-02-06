package io.github.sixcrabs.slardar.core.domain;

import io.github.sixcrabs.slardar.core.AccountStatus;
import io.github.sixcrabs.slardar.core.domain.core.BaseRealmEntity;

import java.time.LocalDateTime;

/**
 * <p>
 * 账号
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/9
 */
public class Account extends BaseRealmEntity<String> implements Cloneable {


    /**
     * 账号名称
     */
    private String name;

    /**
     * 账号密码
     */
    private String password;

    /**
     * openid
     */
    private String openId;

    /**
     * 当前账号访问状态
     */
    private AccountStatus status;

    /**
     * 过期时间,为null 则表示不过期
     */
    private LocalDateTime expireAt;


    /**
     * 密码口令剩余有效天数
     * 若应用方返回该参数，则会进行相应判断 置空则不会判断
     */
    private Integer pwdValidRemainDays;

    /**
     * 账号对应的用户信息
     */
    private UserProfile userProfile;


    public Integer getPwdValidRemainDays() {
        return pwdValidRemainDays;
    }

    public Account setPwdValidRemainDays(Integer pwdValidRemainDays) {
        this.pwdValidRemainDays = pwdValidRemainDays;
        return this;
    }

    /**
     * 是否可用
     *
     */
    public boolean isAccessible() {
        return AccountStatus.accessible.equals(this.status);
    }

    public boolean isExpired() {
        return AccountStatus.expired.equals(this.status);
    }

    public boolean isLocked() {
        return AccountStatus.locked.equals(this.status);
    }


    public String getPassword() {
        return password;
    }

    public Account setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getOpenId() {
        return openId;
    }

    public Account setOpenId(String openId) {
        this.openId = openId;
        return this;
    }

    public String getName() {
        return name;
    }

    public Account setName(String name) {
        this.name = name;
        return this;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Account setStatus(AccountStatus status) {
        this.status = status;
        return this;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    public Account setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
        return this;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public Account setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        return this;
    }

    @Override
    public Account clone() {
        try {
            Account clone = (Account) super.clone();
            clone.setName(this.name)
                    .setPassword(this.password)
                    .setStatus(this.status)
                    .setExpireAt(this.expireAt)
                    .setOpenId(this.openId)
                    .setPwdValidRemainDays(this.pwdValidRemainDays)
                    .setUserProfile(this.userProfile.clone());
            clone.setId(this.getId());
            clone.setDeleted(this.isDeleted() ? 1 : 0);
            clone.setRealm(this.getRealm());
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}