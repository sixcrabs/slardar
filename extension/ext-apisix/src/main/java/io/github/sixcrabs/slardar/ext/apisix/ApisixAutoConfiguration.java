package io.github.sixcrabs.slardar.ext.apisix;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import io.github.sixcrabs.slardar.starter.config.SlardarBeanConfiguration;
import io.github.sixcrabs.slardar.starter.config.SlardarSecurityConfiguration;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/12/16
 */
@AutoConfiguration(after = SlardarBeanConfiguration.class, before = SlardarSecurityConfiguration.class)
@EnableConfigurationProperties(SlardarApisixProperties.class)
public class ApisixAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "slardar.apisix", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ApisixVerifyFilter apisixVerifyFilter(SlardarApisixProperties properties) {
        return new ApisixVerifyFilter(properties);
    }
}