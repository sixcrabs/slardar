package io.github.sixcrabs.slardar.core;

/**
 * <p>
 * 账户状态
 * </p>
 *
 * @author Alex
 * @version v1.0 2022/12/12
 */
public enum AccountStatus {

    /**
     * 可访问的
     */
    accessible("可访问"),
    /**
     * 禁止访问的
     */
    forbidden("禁止访问"),
    /**
     * 账户已到期
     */
    expired("账户已到期"),
    /**
     * 账户已锁定
     */
    locked("账户已锁定");

    private String desc;

    public String getDesc() {
        return desc;
    }

    AccountStatus(String desc) {
        this.desc = desc;
    }
}