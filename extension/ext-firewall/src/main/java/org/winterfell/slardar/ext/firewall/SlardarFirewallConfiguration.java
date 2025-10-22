package org.winterfell.slardar.ext.firewall;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.winterfell.slardar.core.SlardarContext;
import org.winterfell.slardar.starter.config.SlardarBeanConfiguration;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/10/22
 */
@AutoConfiguration(after = SlardarBeanConfiguration.class)
@ConfigurationPropertiesScan("org.winterfell.slardar.ext.firewall")
public class SlardarFirewallConfiguration {


    @Bean
    public SlardarFirewallFilter slardarFirewallFilter(SlardarContext context) {
        return new SlardarFirewallFilter(context);
    }
}
