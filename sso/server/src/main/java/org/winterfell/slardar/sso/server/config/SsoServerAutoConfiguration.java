package org.winterfell.slardar.sso.server.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.winterfell.slardar.core.SlardarContext;
import org.winterfell.slardar.spi.SlardarSpiFactory;
import org.winterfell.slardar.sso.server.SsoServerRequestFilter;
import org.winterfell.slardar.sso.server.SsoServerRequestHandler;
import org.winterfell.slardar.sso.server.SsoTicketService;
import org.winterfell.slardar.starter.config.SlardarBeanConfiguration;
import org.winterfell.slardar.starter.SlardarProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * .TODO
 *
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
@AutoConfiguration(after = SlardarBeanConfiguration.class)
@EnableConfigurationProperties(SsoServerProperties.class)
public class SsoServerAutoConfiguration {

    @Bean
    public SsoTicketService ssoTicketService(SlardarSpiFactory spiFactory, SlardarProperties slardarProperties,
                                             SsoServerProperties serverProperties) {
        return new SsoTicketService(spiFactory, slardarProperties, serverProperties);
    }

    @Bean
    public SsoServerRequestHandler ssoServerRequestHandler(SsoServerProperties serverProperties, SlardarContext context,
                                                  SsoTicketService ssoTicketService) {
        return new SsoServerRequestHandler(serverProperties, context, ssoTicketService);
    }

    @Bean
    public SsoServerRequestFilter ssoServerRequestFilter(SsoServerProperties properties,
                                                      SsoServerRequestHandler requestHandler) {
        return new SsoServerRequestFilter(properties, requestHandler);
    }

}
