package cn.piesat.nj.slardar.sso.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static cn.piesat.nj.slardar.sso.server.SsoConstants.SSO_CTX_PATH;

/**
 * <p>
 *     TODO:
 * sso server 配置属性
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
@ConfigurationProperties(prefix = "slardar.sso-server")
public class SsoServerProperties {

    /**
     * context path
     */
    private String ctxPath = SSO_CTX_PATH;


    public String getCtxPath() {
        return ctxPath;
    }

    public void setCtxPath(String ctxPath) {
        this.ctxPath = ctxPath;
    }
}
