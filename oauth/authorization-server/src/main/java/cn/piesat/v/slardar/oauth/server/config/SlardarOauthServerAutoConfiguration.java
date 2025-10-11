package cn.piesat.v.slardar.oauth.server.config;

import cn.piesat.v.slardar.oauth.server.OauthServerRequestFilter;
import cn.piesat.v.slardar.oauth.server.OauthServerRequestHandler;
import org.winterfell.slardar.spi.SlardarSpiContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * 自动配置 oauth server
 * </p>
 *
 * @author Alex
 * @since 2025/4/13
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OauthServerProperties.class)
public class SlardarOauthServerAutoConfiguration {

    @Bean
    public OauthServerRequestHandler requestHandler(OauthServerProperties serverProperties, SlardarSpiContext context) {
        return new OauthServerRequestHandler(serverProperties, context);
    }

    @Bean
    public OauthServerRequestFilter oauthServerRequestFilter(OauthServerProperties properties,
                                                             OauthServerRequestHandler requestHandler) {
        return new OauthServerRequestFilter(properties, requestHandler);
    }
}
