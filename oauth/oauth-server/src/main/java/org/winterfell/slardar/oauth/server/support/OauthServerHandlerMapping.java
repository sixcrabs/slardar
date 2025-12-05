package org.winterfell.slardar.oauth.server.support;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
public enum OauthServerHandlerMapping {
    // 授权码模式
    authorize,
    // 凭证式
    token,

    // 刷新令牌
    refresh,
    // 撤销令牌
    revoke,

    client_token,

    // 用户详情
    profile,
    userDetails,


}