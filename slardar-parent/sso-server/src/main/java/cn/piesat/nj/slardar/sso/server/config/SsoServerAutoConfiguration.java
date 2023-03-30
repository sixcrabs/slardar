package cn.piesat.nj.slardar.sso.server.config;

import cn.piesat.nj.slardar.sso.server.SsoServerRequestFilter;
import cn.piesat.nj.slardar.sso.server.SsoServerRequestHandler;
import cn.piesat.nj.slardar.sso.server.SsoTicketService;
import cn.piesat.nj.slardar.starter.SlardarContext;
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
public class SsoServerAutoConfiguration {

    @Bean
    public SsoTicketService ssoTicketService(SsoServerProperties serverProperties) {
        return new SsoTicketService(serverProperties);
    }

    @Bean
    public SsoServerRequestHandler requestHandler(SsoServerProperties serverProperties, SlardarContext context,
                                                  SsoTicketService ssoTicketService) {
        return new SsoServerRequestHandler(serverProperties, context, ssoTicketService);
    }

    @Bean
    public SsoServerRequestFilter serverRequestFilter(SsoServerProperties properties,
                                                      SsoServerRequestHandler requestHandler) {
        return new SsoServerRequestFilter(properties, requestHandler);
    }

}
