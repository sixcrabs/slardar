package cn.piesat.nj.slardar.core.entity;

import cn.piesat.nj.slardar.core.entity.core.BaseRealmEntity;

import java.time.LocalDateTime;

/**
 * <p>
 * 审计日志
 * - 什么账号什么人登录过
 * - 从什么地方登录(终端类型,ip)
 * - 什么时间登录什么系统
 * - 什么时间退出
 * 登录后审计:
 * - 登录后访问哪些资源（访问了哪些接口..）
 * - 对资源做了什么操作(delete/update/insert...)
 * - ...
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/9
 */
public class AuditLog extends BaseRealmEntity<String> {

    private String accountId;

    /**
     * 冗余 用户信息
     */
    private String userProfileId;

    /**
     * 账户名
     */
    private String accountName;

    /**
     * 冗余用户名
     */
    private String userProfileName;

    /**
     * 客户端类型 pc app
     */
    private String clientType;

    /**
     * 客户端ip
     */
    private String clientIp;

    /**
     * 操作类型
     * - login
     * - logout
     * - access
     * - get profile 获取信息
     */
    private String logType;

    /**
     * 记录时间
     */
    private LocalDateTime logTime;

    /**
     * 详细操作内容 可为空
     */
    private String detail;


    public String getDetail() {
        return detail;
    }

    public AuditLog setDetail(String detail) {
        this.detail = detail;
        return this;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "accountId='" + accountId + '\'' +
                ", userProfileId='" + userProfileId + '\'' +
                ", accountName='" + accountName + '\'' +
                ", userProfileName='" + userProfileName + '\'' +
                ", clientType='" + clientType + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", logType='" + logType + '\'' +
                ", logTime=" + logTime +
                '}';
    }

    public String getAccountId() {
        return accountId;
    }

    public AuditLog setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getUserProfileId() {
        return userProfileId;
    }

    public AuditLog setUserProfileId(String userProfileId) {
        this.userProfileId = userProfileId;
        return this;
    }

    public String getClientType() {
        return clientType;
    }

    public AuditLog setClientType(String clientType) {
        this.clientType = clientType;
        return this;
    }

    public String getClientIp() {
        return clientIp;
    }

    public AuditLog setClientIp(String clientIp) {
        this.clientIp = clientIp;
        return this;
    }

    public String getLogType() {
        return logType;
    }

    public AuditLog setLogType(String logType) {
        this.logType = logType;
        return this;
    }

    public LocalDateTime getLogTime() {
        return logTime;
    }

    public AuditLog setLogTime(LocalDateTime logTime) {
        this.logTime = logTime;
        return this;
    }

    public String getAccountName() {
        return accountName;
    }

    public AuditLog setAccountName(String accountName) {
        this.accountName = accountName;
        return this;
    }

    public String getUserProfileName() {
        return userProfileName;
    }

    public AuditLog setUserProfileName(String userProfileName) {
        this.userProfileName = userProfileName;
        return this;
    }
}
