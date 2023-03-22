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
     * sso-server url
     */
    private String serverUrl;


    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
}
