package cn.piesat.v.slardar.oauth.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/4/13
 */
public final class OauthConstants {

    public static final Gson GSON = new GsonBuilder().create();

    /**
     * 默认的oauth2请求的上下文路径 可以通过配置文件进行修改
     */
    public static final String OAUTH_CTX_PATH = "/oauth";
}
