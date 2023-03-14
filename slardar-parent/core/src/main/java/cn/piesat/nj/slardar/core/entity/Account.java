package cn.piesat.nj.slardar.core.entity;

import cn.piesat.nj.slardar.core.AccountStatus;
import cn.piesat.nj.slardar.core.entity.core.BaseRealmEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 账号
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/9
 */
public class Account extends BaseRealmEntity<String> {


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
     * 账号对应的用户信息
     */
    private UserProfile userProfile;


    /**
     * 是否可用
     * @return
     */
    public boolean isAccessible() {
        return AccountStatus.accessible.equals(this.status);
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
}
