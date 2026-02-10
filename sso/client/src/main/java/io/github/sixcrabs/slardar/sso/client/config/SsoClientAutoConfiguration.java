package io.github.sixcrabs.slardar.sso.client.config;

import io.github.sixcrabs.slardar.sso.client.SsoClientRequestFilter;
import io.github.sixcrabs.slardar.sso.client.SsoClientRequestHandler;
import io.github.sixcrabs.slardar.sso.client.config.client.SsoServerApiClient;
import io.github.sixcrabs.winterfell.remote.mrc.EnableMrClients;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
@Configuration
@EnableMrClients(clients = SsoServerApiClient.class)
@EnableConfigurationProperties(SsoClientProperties.class)
public class SsoClientAutoConfiguration {


    @Bean
    public SsoClientRequestHandler requestHandler(SsoClientProperties clientProperties) {
        return new SsoClientRequestHandler(clientProperties);
    }
    @Bean
    public SsoClientRequestFilter requestFilter(SsoClientRequestHandler requestHandler, SsoClientProperties clientProperties) {
        return new SsoClientRequestFilter(requestHandler, clientProperties);
    }

}