package cn.piesat.nj.slardar.sso.server.config;

import cn.piesat.nj.slardar.sso.server.SsoServerRequestFilter;
import cn.piesat.nj.slardar.sso.server.SsoServerRequestHandler;
import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.config.SlardarIgnoringCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.WebSecurity;

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
    public SsoServerRequestHandler requestHandler(SsoServerProperties serverProperties, SlardarContext context) {
        return new SsoServerRequestHandler(serverProperties, context);
    }

    @Bean
    public SsoServerRequestFilter serverRequestFilter(SsoServerProperties properties,
                                                      SsoServerRequestHandler requestHandler) {
        return new SsoServerRequestFilter(properties, requestHandler);
    }

}
