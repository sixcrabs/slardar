package cn.piesat.v.slardar.sso.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

import static cn.piesat.v.slardar.sso.server.support.SsoConstants.SSO_CTX_PATH;

/**
 * <p>
 * sso server 配置属性
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
@ConfigurationProperties(prefix = "slardar.sso")
public class SsoServerProperties {

    /**
     * context path
     */
    private String ctxPath = SSO_CTX_PATH;


    /**
     * ticket setting
     */
    private TicketSetting ticket = new TicketSetting();


    public static class TicketSetting {

        /**
         * 默认长度 12
         */
        private int length = 12;

        /**
         * 默认有效期 60s
         */
        private Duration ttl = Duration.ofSeconds(30);

        public int getLength() {
            return length;
        }

        public TicketSetting setLength(int length) {
            this.length = length;
            return this;
        }

        public Duration getTtl() {
            return ttl;
        }

        public TicketSetting setTtl(Duration ttl) {
            this.ttl = ttl;
            return this;
        }
    }

    public TicketSetting getTicket() {
        return ticket;
    }

    public SsoServerProperties setTicket(TicketSetting ticket) {
        this.ticket = ticket;
        return this;
    }

    public String getSsoAntUrlPattern() {
        return this.ctxPath.endsWith("/") ? this.ctxPath.concat("**") : this.ctxPath.concat("/**");
    }


    public String getCtxPath() {
        return ctxPath;
    }

    public void setCtxPath(String ctxPath) {
        this.ctxPath = ctxPath;
    }

}
