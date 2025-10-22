package org.winterfell.slardar.ext.firewall;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.winterfell.slardar.ext.firewall.core.handlers.SlardarFirewallBlackPathsHandler;
import org.winterfell.slardar.ext.firewall.core.handlers.SlardarFirewallHeadersHandler;
import org.winterfell.slardar.ext.firewall.core.handlers.SlardarFirewallHostsHandler;

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
