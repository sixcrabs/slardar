package cn.piesat.nj.slardar.starter.support;

import java.util.Map;

/**
 * 封装 auth 认证请求参数
 *
 * @author alex
 * @version v1.0 2021/8/28
 */
public class AuthxRequestWrapper {

    private Map<String, String> requestParams;

    private String sessionId;

    private Map<String,String> requestHeaders;

    public Map<String, String> getRequestParams() {
        return requestParams;
    }

    public AuthxRequestWrapper setRequestParams(Map<String, String> requestParams) {
        this.requestParams = requestParams;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public AuthxRequestWrapper setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public AuthxRequestWrapper setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
        return this;
    }
}
