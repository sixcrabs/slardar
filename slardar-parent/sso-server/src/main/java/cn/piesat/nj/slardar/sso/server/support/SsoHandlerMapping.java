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
    @Deprecated
            // 这里rest 登录沿用 slardar 自身的 /login 接口
    login,
    logout,
    // 验证 ticket
    checkTicket


}
