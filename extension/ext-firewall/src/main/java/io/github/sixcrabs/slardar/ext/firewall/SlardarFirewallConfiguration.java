package io.github.sixcrabs.slardar.ext.firewall;

import io.github.sixcrabs.slardar.core.SlardarContext;
import io.github.sixcrabs.slardar.starter.config.SlardarBeanConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/10/22
 */
@AutoConfiguration(after = SlardarBeanConfiguration.class)
@ConfigurationPropertiesScan("io.github.sixcrabs.slardar.ext.firewall")
public class SlardarFirewallConfiguration {


    @Bean
    public SlardarFirewallFilter slardarFirewallFilter(SlardarContext context) {
        return new SlardarFirewallFilter(context);
    }
}