package cn.piesat.v.slardar.sso.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
@ConfigurationProperties(prefix = "slardar.sso")
public class SsoClientProperties {

    /**
     * context path
     */
    private String ctxPath = "/sso";

    /**
     * sso-server url
     * eg: `http://127.0.0.1:8000/sso`
     */
    private String serverUrl;

    /**
     * 忽略的url
     */
    private String[] ignores = new String[]{};

    public String[] getIgnores() {
        return ignores;
    }

    public SsoClientProperties setIgnores(String[] ignores) {
        this.ignores = ignores;
        return this;
    }

    public String getCtxPath() {
        return ctxPath;
    }

    public SsoClientProperties setCtxPath(String ctxPath) {
        this.ctxPath = ctxPath;
        return this;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
}
