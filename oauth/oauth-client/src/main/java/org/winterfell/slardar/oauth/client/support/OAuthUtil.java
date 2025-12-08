package org.winterfell.slardar.oauth.client.support;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.winterfell.misc.hutool.mini.URLUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.winterfell.misc.hutool.mini.StringUtil.isEmpty;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/12/5
 */
public final class OAuthUtil {

    public static final Gson GSON = new Gson();

    /**
     * get with headers
     *
     * @param url
     * @param headers
     * @return
     */
    public static String get(String url, Map<String, String> headers) {
        try {
            Request request = Request.get(url);
            if (headers != null) {
                headers.forEach(request::addHeader);
            }
            return request.execute().returnContent().asString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get 获取 jsonObject
     *
     * @param url
     * @param headers
     * @return
     */
    public static JsonObject getAndParse(String url, Map<String, String> headers) {
        try {
            String res = get(url, headers);
            return GSON.fromJson(res, JsonObject.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * post 请求 返回 string
     *
     * @param url
     * @param params
     * @return
     */
    public static String post(String url, Map<String, Object> params) {
        try {
            Request request = Request.post(url);
            if (params != null) {
                request.bodyString(GSON.toJson(params), ContentType.APPLICATION_JSON);
            }
           return request.execute().returnContent().asString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * post 请求 获取 jsonObject
     *
     * @param url
     * @param params
     * @return
     */
    public static JsonObject postAndParse(String url, Map<String, Object> params) {
        try {
            String res = post(url, params);
            return GSON.fromJson(res, JsonObject.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }
}