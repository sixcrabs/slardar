package cn.piesat.v.slardar.license.cli;

/**
 * <p>
 * 客户信息的key枚举类
 * </p>
 *
 * @author Alex
 * @since 2025/9/18
 */
public enum CustomerKey {

    /**
     * 客户名称
     */
    name,

    /**
     * 客户到期时间
     */
    expired,

    /**
     * 客户邮箱
     */
    email,

    /**
     * 客户地址
     */
    address,

    /**
     * 客户联系方式
     */
    contact,

    /**
     * 客户机器码(用于绑定安装机器，暂未启用)
     */
    machineCode,

    /**
     * 产品码(用于绑定授权的产品，暂未启用)
     */
    productCode,

}
