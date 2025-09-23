package cn.piesat.v.slardar.license.manager.config;

import cn.piesat.v.slardar.license.manager.LicenseManageRequestFilter;
import cn.piesat.v.slardar.license.manager.LicenseManageRequestHandler;
import cn.piesat.v.slardar.spi.SlardarSpiFactory;
import cn.piesat.v.slardar.starter.config.SlardarBeanConfiguration;
import cn.piesat.v.slardar.starter.config.SlardarProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * auto configure for license manager
 * </p>
 *
 * @author Alex
 * @since 2025/9/22
 */
@Configuration
@EnableConfigurationProperties(SlardarLicenseProperties.class)
@AutoConfigureAfter(SlardarBeanConfiguration.class)
public class SlardarLicenseConfiguration {


    @Bean
    public LicenseManageRequestHandler requestHandler(SlardarLicenseProperties properties,
                                                      SlardarProperties slardarProperties, SlardarSpiFactory spiFactory) {
        return new LicenseManageRequestHandler(properties, slardarProperties, spiFactory);
    }
    @Bean
    public LicenseManageRequestFilter requestFilter(LicenseManageRequestHandler requestHandler) {
        return new LicenseManageRequestFilter(requestHandler);
    }
}
