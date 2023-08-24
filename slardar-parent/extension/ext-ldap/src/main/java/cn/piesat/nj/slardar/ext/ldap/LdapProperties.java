package cn.piesat.nj.slardar.ext.ldap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/24
 */
@ConfigurationProperties(prefix = "slardar.ldap")
public class LdapProperties {

    private static final int DEFAULT_PORT = 389;

    private int port = DEFAULT_PORT;
    /**
     * ldap://xxx:389
     */
    private String url;
    /**
     * base DN, eg: dc=test,dc=cn
     */
    private String base;

    private Boolean anonymousReadOnly;

    private final Map<String, String> baseEnvironment = new HashMap();


    public String determineUrls() {
        return ObjectUtils.isEmpty(this.url) ? "ldap://localhost:" + port : this.url;
    }


    public int getPort() {
        return port;
    }

    public LdapProperties setPort(int port) {
        this.port = port;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public LdapProperties setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getBase() {
        return base;
    }

    public LdapProperties setBase(String base) {
        this.base = base;
        return this;
    }


    public Boolean getAnonymousReadOnly() {
        return anonymousReadOnly;
    }

    public LdapProperties setAnonymousReadOnly(Boolean anonymousReadOnly) {
        this.anonymousReadOnly = anonymousReadOnly;
        return this;
    }

    public Map<String, String> getBaseEnvironment() {
        return baseEnvironment;
    }

}
