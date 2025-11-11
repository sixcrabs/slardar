package org.winterfell.slardar.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * .
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/9
 */
public final class Constants {

    public static final String REALM_EMPTY = "";

    public static final String BEARER = "Bearer ";

    /**
     * 默认登录地址
     */
    public static final String AUTH_LOGIN_URL = "/login";

    /**
     * 默认登出地址
     */
    public static final String AUTH_LOGOUT_URL = "/logout";

    /**
     * 默认 验证码地址
     */
    public static final String CAPTCHA_URL = "/captcha";

    /**
     * 提供rest 方式获取用户详情的url
     */
    public static final String AUTH_USER_DETAILS_URL = "/userdetails";

    /**
     * 默认 token key
     */
    public static final String AUTH_TOKEN_KEY = "Authorization";

    /**
     * 请求头 的 租户key
     */
    public static final String HEADER_KEY_OF_REALM = "X-realm";

    /**
     * 认证方式
     * wxapp / password
     */
    public static final String HEADER_KEY_OF_AUTH_TYPE = "X-Auth-Type";

    public static final List<String> HEADER_KEYS_OF_AUTH_TYPE = Collections.unmodifiableList(Arrays.asList(HEADER_KEY_OF_AUTH_TYPE,
            HEADER_KEY_OF_AUTH_TYPE.toLowerCase(), HEADER_KEY_OF_AUTH_TYPE.toUpperCase(),
            "x-Auth-Type", "X-auth-type", "X-Auth-type", "X-auth-Type"));

    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";


    /**
     * 微信openid 认证类型
     */
    public static final String AUTH_TYPE_WX_APP = "wxapp";

    public static final String AUTH_TYPE_NORMAL = "password";

    public static final String ANONYMOUS = "anonymous";

    /**
     * 定义移动端请求的所有可能类型
     */
    public final static String[] MOBILE_AGENTS = {"Android", "iPhone", "iPod", "iPad", "Windows Phone", "MQQBrowser"};

    public static final String TOKEN_REQUIRED = "token is required";
    public static final String TOKEN_EXPIRED = "token has been expired";

    public static final String KEY_PREFIX_CAPTCHA = "slardar:captcha:";
    public static final String KEY_PREFIX_TOKEN = "slardar:token:";

}