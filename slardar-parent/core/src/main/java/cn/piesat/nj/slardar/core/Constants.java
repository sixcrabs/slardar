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

    public static final String AUTHORIZATION_HEAD = "Authorization";

    public static final String BEARER = "Bearer ";

    /**
     * 默认登录地址
     */
    public static final String AUTH_LOGIN_URL = "/login";

    /**
     * 认证方式
     * wxapp / password
     */
    public static final String AUTH_TYPE_HEADER_KEY = "Auth-Type";


    /**
     * 微信openid 认证类型
     */
    public static final String AUTH_TYPE_WX_APP = "wxapp";

    public static final String ANONYMOUS = "anonymous";

    /**
     * 定义移动端请求的所有可能类型
     */
    public final static String[] MOBILE_AGENTS = {"Android", "iPhone", "iPod", "iPad", "Windows Phone", "MQQBrowser"};
}
