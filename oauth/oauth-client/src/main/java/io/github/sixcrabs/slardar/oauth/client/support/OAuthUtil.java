package io.github.sixcrabs.slardar.oauth.client.support;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/12/5
 */
@Slf4j
public final class OAuthUtil {

    public static final Gson GSON = new Gson();

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
     * get with headers
     *
     * @param url
     * @param headers
     * @return
     */
    public static String get(String url, Map<String, String> headers) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request.Builder builder = new Request.Builder().url(url).get();
            if (headers != null) {
                headers.forEach(builder::addHeader);
            }
            try (Response resp = client.newCall(builder.build()).execute()) {
                return resp.body().string(); // JSON
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * post 请求 返回 string
     *
     * @param url
     * @param params
     * @param headers
     * @return
     */
    public static String post(String url, Map<String, Object> params, Map<String, String> headers) {
        try {
            OkHttpClient client = new OkHttpClient();
            okhttp3.Request.Builder builder = new Request.Builder().url(url)
                    .post(RequestBody.create("", MediaType.parse("application/json")));
            if (params != null) {
                MediaType JSON = MediaType.get("application/json; charset=utf-8");
                String json = GSON.toJson(params);
                RequestBody body = RequestBody.create(json, JSON);
                builder.post(body);
            }
            if (headers != null) {
                headers.forEach(builder::addHeader);
            }
            try (Response resp = client.newCall(builder.build()).execute()) {
                return resp.body().string();
            }
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
    public static JsonObject postAndParse(String url, Map<String, Object> params, Map<String, String> headers) {
        try {
            String res = post(url, params, headers);
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