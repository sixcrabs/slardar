package cn.piesat.nj.slardar.sso.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
@ConfigurationProperties(prefix = "slardar.sso-client")
public class SsoClientProperties {

    /**
     * context path
     */
    private String ctxPath = "/sso";

    /**
     * sso-server url
     * egL http://xxx/sso
     */
    private String serverUrl;


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
