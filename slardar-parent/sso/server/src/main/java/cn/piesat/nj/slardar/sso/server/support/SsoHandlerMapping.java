package cn.piesat.nj.slardar.sso.server.support;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
public enum SsoHandlerMapping {
    //
    auth,
    // 单点注销
    logout,
    // 验证 ticket
    checkTicket,
    checkticket,

    // 用户详情
    userdetails,
    userDetails,

    // 验证token是否有效
    validatetoken,
    validateToken;


}
