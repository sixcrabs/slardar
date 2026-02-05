package io.github.sixcrabs.slardar.sso.server.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import io.github.sixcrabs.slardar.core.SlardarContext;
import io.github.sixcrabs.slardar.spi.SlardarSpiFactory;
import io.github.sixcrabs.slardar.sso.server.SsoServerRequestFilter;
import io.github.sixcrabs.slardar.sso.server.SsoServerRequestHandler;
import io.github.sixcrabs.slardar.sso.server.SsoTicketService;
import io.github.sixcrabs.slardar.starter.config.SlardarBeanConfiguration;
import io.github.sixcrabs.slardar.starter.SlardarProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

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