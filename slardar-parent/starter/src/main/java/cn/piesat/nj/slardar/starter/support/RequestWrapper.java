package cn.piesat.nj.slardar.starter.support;


import java.util.Map;

/**
 * <p>
 * .wrapper for request
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/29
 */
public class RequestWrapper {


    private Map<String, String> requestParams;

    private String sessionId;

    private Map<String, String> requestHeaders;

    private LoginDeviceType loginDeviceType;

    public Map<String, String> getRequestParams() {
        return requestParams;
    }

    public RequestWrapper setRequestParams(Map<String, String> requestParams) {
        this.requestParams = requestParams;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public RequestWrapper setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public RequestWrapper setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
        return this;
    }

    public LoginDeviceType getLoginDeviceType() {
        return loginDeviceType;
    }

    public RequestWrapper setLoginDeviceType(LoginDeviceType loginDeviceType) {
        this.loginDeviceType = loginDeviceType;
        return this;
    }
}
