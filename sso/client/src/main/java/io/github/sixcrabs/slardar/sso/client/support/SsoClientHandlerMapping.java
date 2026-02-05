package io.github.sixcrabs.slardar.sso.client.support;

/**
 * <p>
 * .
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/30
 */
public enum SsoClientHandlerMapping {

    // 是否已登录
    isLogin,

    // 获取 sso server url
    getSsoAuthUrl,

    // 使用 ticket 登录
    doLogin,

    // 退出
    doLogout;




}