package org.winterfell.slardar.oauth.server.support;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/10/13
 */
public final class OauthConstants {


    /**
     * 请求参数名
     */
    public static final class RequestParam {
        public static String response_type = "response_type";
        public static String client_id = "client_id";
        public static String client_secret = "client_secret";
        public static String redirect_uri = "redirect_uri";
        public static String scope = "scope";
        public static String state = "state";
        public static String code = "code";
        public static String token = "token";
        public static String access_token = "access_token";
        public static String refresh_token = "refresh_token";
        public static String grant_type = "grant_type";
        public static String username = "username";
        public static String password = "password";
        public static String name = "name";
        public static String pwd = "pwd";

    }

    /**
     * 响应类型
     */
    public static final class ResponseType {
        // 授权码模式
        public static String code = "code";
        // 隐藏式
        @Deprecated
        public static String token = "token";
    }

    /**
     * 授权类型
     */
    public static final class GrantType {
        public static String authorization_code = "authorization_code";
        public static String refresh_token = "refresh_token";
        public static String client_credentials = "client_credentials";
        @Deprecated
        public static String password = "password";
        @Deprecated
        public static String implicit = "implicit";
    }
}
