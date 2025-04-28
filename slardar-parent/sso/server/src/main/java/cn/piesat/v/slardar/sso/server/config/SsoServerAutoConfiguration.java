package cn.piesat.v.slardar.sso.server.config;

import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.spi.SlardarSpiFactory;
import cn.piesat.v.slardar.sso.server.SsoServerRequestFilter;
import cn.piesat.v.slardar.sso.server.SsoServerRequestHandler;
import cn.piesat.v.slardar.sso.server.SsoTicketService;
import cn.piesat.v.slardar.starter.config.SlardarBeanConfiguration;
import cn.piesat.v.slardar.starter.config.SlardarProperties;
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
@Configuration
@EnableConfigurationProperties(SsoServerProperties.class)
@AutoConfigureAfter(SlardarBeanConfiguration.class)
public class SsoServerAutoConfiguration {

    @Bean
    public SsoTicketService ssoTicketService(SlardarSpiFactory spiFactory, SlardarProperties slardarProperties,
                                             SsoServerProperties serverProperties) {
        return new SsoTicketService(spiFactory, slardarProperties, serverProperties);
    }

    @Bean
    public SsoServerRequestHandler requestHandler(SsoServerProperties serverProperties, SlardarSpiContext context,
                                                  SsoTicketService ssoTicketService) {
        return new SsoServerRequestHandler(serverProperties, context, ssoTicketService);
    }

    @Bean
    public SsoServerRequestFilter serverRequestFilter(SsoServerProperties properties,
                                                      SsoServerRequestHandler requestHandler) {
        return new SsoServerRequestFilter(properties, requestHandler);
    }

}
