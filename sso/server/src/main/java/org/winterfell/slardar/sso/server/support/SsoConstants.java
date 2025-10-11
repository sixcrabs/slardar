package org.winterfell.slardar.sso.server.support;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
public final class SsoConstants {

    public static final Gson GSON = new GsonBuilder().create();

    /**
     * 默认请求路径 应用可在配置里修改
     */
    public static final String SSO_CTX_PATH = "/sso";

    /**
     * 请求中传递 token 的key
     */
    public static final String SSO_TOKEN_KEY = "Authorization";

    /** redirect参数名称 */
    public static final String SSO_PARAM_REDIRECT = "url";

    /** ticket参数名称 */
    public static final String SSO_PARAM_TICKET = "ticket";


    /** 转发失败 */
    public static final int CODE_20001 = 20001;

    /** 重定向失败 */
    public static final int CODE_20002 = 20002;

    /**
     * ticket 验证失败
     */
    public static final int CODE_TICKET_ERROR = 20003;


    /**
     * sso-login 登录页面地址
     */
    public static final String SSO_LOGIN_VIEW_URL = "/sso-login";
}
