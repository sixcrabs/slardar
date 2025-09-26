package cn.piesat.v.slardar.license.manager.config;

import cn.piesat.v.slardar.license.manager.LicenseManageRequestFilter;
import cn.piesat.v.slardar.license.manager.LicenseManageRequestHandler;
import cn.piesat.v.slardar.license.manager.LicenseVerifyFilter;
import cn.piesat.v.slardar.spi.SlardarSpiFactory;
import cn.piesat.v.slardar.starter.config.SlardarBeanConfiguration;
import cn.piesat.v.slardar.starter.config.SlardarProperties;
import cn.piesat.v.slardar.starter.config.SlardarSecurityConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
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
@AutoConfiguration
@EnableConfigurationProperties(SlardarLicenseProperties.class)
@AutoConfigureAfter(SlardarBeanConfiguration.class)
@AutoConfigureBefore(SlardarSecurityConfiguration.class)
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
