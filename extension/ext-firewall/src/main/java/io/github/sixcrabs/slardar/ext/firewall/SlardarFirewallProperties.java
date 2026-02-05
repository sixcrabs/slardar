package io.github.sixcrabs.slardar.ext.firewall;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import io.github.sixcrabs.slardar.ext.firewall.handlers.SlardarFirewallBlackPathsHandler;
import io.github.sixcrabs.slardar.ext.firewall.handlers.SlardarFirewallHeadersHandler;
import io.github.sixcrabs.slardar.ext.firewall.handlers.SlardarFirewallHostsHandler;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/10/22
 */
@ConfigurationProperties(prefix = "slardar.firewall")
@Data
public class SlardarFirewallProperties {

    /**
     * 黑名单path
     */
    private SlardarFirewallBlackPathsHandler.Setting blackPath = new SlardarFirewallBlackPathsHandler.Setting();

    /**
     * 头信息
     */
    private SlardarFirewallHeadersHandler.Setting headers = new SlardarFirewallHeadersHandler.Setting();

    /**
     * host 域名
     */
    public SlardarFirewallHostsHandler.Setting hosts = new SlardarFirewallHostsHandler.Setting();

}