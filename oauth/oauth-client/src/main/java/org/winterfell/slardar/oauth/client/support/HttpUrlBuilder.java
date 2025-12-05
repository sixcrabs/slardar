package org.winterfell.slardar.oauth.client.support;

import lombok.Setter;
import org.winterfell.misc.hutool.mini.MapUtil;
import org.winterfell.misc.hutool.mini.StringUtil;

import java.util.*;

import static org.h2.util.StringUtils.urlEncode;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/12/5
 */
@Setter
public class HttpUrlBuilder {

    private final Map<String, String> params = new LinkedHashMap<>(7);
    private String baseUrl;


    /**
     * @param baseUrl 基础路径
     * @return the new {@code UrlBuilder}
     */
    public static HttpUrlBuilder fromBaseUrl(String baseUrl) {
        HttpUrlBuilder builder = new HttpUrlBuilder();
        builder.setBaseUrl(baseUrl);
        return builder;
    }

    /**
     * 只读的参数Map
     *
     * @return unmodifiable Map
     * @since 1.15.0
     */
    public Map<String, Object> getReadOnlyParams() {
        return Collections.unmodifiableMap(params);
    }

    /**
     * 添加参数
     *
     * @param key   参数名称
     * @param value 参数值
     * @return this UrlBuilder
     */
    public HttpUrlBuilder queryParam(String key, Object value) {
        if (StringUtil.isEmpty(key)) {
            throw new RuntimeException("参数名不能为空");
        }
        String valueAsString = (value != null ? value.toString() : null);
        this.params.put(key, valueAsString);

        return this;
    }

    /**
     * 构造url
     *
     * @return url
     */
    public String build() {
        return this.build(false);
    }

    /**
     * 构造url
     *
     * @param encode 转码
     * @return url
     */
    public String build(boolean encode) {
        if (MapUtil.isEmpty(this.params)) {
            return this.baseUrl;
        }
        String baseUrl = StringUtil.appendIfMissing(this.baseUrl, "?", "&");
        String paramString = parseMapToString(this.params, encode);
        return baseUrl + paramString;
    }

    /**
     * map转字符串，转换后的字符串格式为 {@code xxx=xxx&xxx=xxx}
     *
     * @param params 待转换的map
     * @param encode 是否转码
     * @return str
     */
    private String parseMapToString(Map<String, String> params, boolean encode) {
        List<String> paramList = new ArrayList<>();
        params.forEach((k, v) -> {
            if (v == null) {
                paramList.add(k + "=");
            } else {
                paramList.add(k + "=" + (encode ? urlEncode(v) : v));
            }
        });
        return String.join("&", paramList);
    }
}