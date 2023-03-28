package cn.piesat.nj.slardar.core;

/**
 * <p>
 * .
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/9
 */
public final class Constants {

    public static final String REALM_MASTER = "master";

    public static final String BEARER = "Bearer ";

    /**
     * 默认登录地址
     */
    public static final String AUTH_LOGIN_URL = "/login";

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
}
