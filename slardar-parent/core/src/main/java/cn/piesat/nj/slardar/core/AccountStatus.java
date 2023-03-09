package cn.piesat.nj.slardar.core;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2022/12/12
 */
public enum AccountStatus {

    //
    accessible("可访问的"),
    forbidden("禁止访问的"),
    expired("访问已到期的"),
    locked("已被锁定的");

    private String desc;

    public String getDesc() {
        return desc;
    }

    AccountStatus(String desc) {
        this.desc = desc;
    }
}
