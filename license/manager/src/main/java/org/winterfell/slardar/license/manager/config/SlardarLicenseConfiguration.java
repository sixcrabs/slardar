package org.winterfell.slardar.license.manager.config;

import org.winterfell.slardar.license.manager.LicenseManageRequestFilter;
import org.winterfell.slardar.license.manager.LicenseManageRequestHandler;
import org.winterfell.slardar.license.manager.LicenseVerifyFilter;
import org.winterfell.slardar.spi.SlardarSpiFactory;
import org.winterfell.slardar.starter.config.SlardarBeanConfiguration;
import org.winterfell.slardar.starter.SlardarProperties;
import org.winterfell.slardar.starter.config.SlardarSecurityConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

/**
 * <p>
 * auto configure for license manager
 * </p>
 *
 * @author Alex
 * @since 2025/9/22
 */
@AutoConfiguration(after = SlardarBeanConfiguration.class, before = SlardarSecurityConfiguration.class)
@EnableConfigurationProperties(SlardarLicenseProperties.class)
public class SlardarLicenseConfiguration {


    @Bean
    public LicenseManageRequestHandler licenseRequestHandler(SlardarLicenseProperties properties,
                                                             SlardarProperties slardarProperties, SlardarSpiFactory spiFactory) {
        return new LicenseManageRequestHandler(properties, slardarProperties, spiFactory);
    }

    @Bean
    public LicenseManageRequestFilter licenseRequestFilter(LicenseManageRequestHandler requestHandler) {
        return new LicenseManageRequestFilter(requestHandler);
    }

    @Lazy(value = false)
    @Bean
    public LicenseVerifyFilter licenseVerifyFilter(LicenseManageRequestHandler requestHandler) {
        return new LicenseVerifyFilter(requestHandler);
    }
}
