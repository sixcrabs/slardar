package io.github.sixcrabs.slardar.oauth.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static io.github.sixcrabs.slardar.oauth.server.support.OauthServerUtil.OAUTH_CTX_PATH;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/4/13
 */
@ConfigurationProperties(prefix = "slardar.oauth")
public class OauthServerProperties {

    private String ctxPath = OAUTH_CTX_PATH;


    public String getOauthAntUrlPattern() {
        return this.ctxPath.endsWith("/") ? this.ctxPath.concat("**") : this.ctxPath.concat("/**");
    }

    public String getCtxPath() {
        return ctxPath;
    }

    public void setCtxPath(String ctxPath) {
        this.ctxPath = ctxPath;
    }
}