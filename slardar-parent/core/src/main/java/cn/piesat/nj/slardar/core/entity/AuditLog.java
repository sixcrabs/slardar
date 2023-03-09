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

}
