package cn.piesat.v.slardar.oauth.server.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * 自动配置 oauth server
 * TODO
 * </p>
 *
 * @author Alex
 * @since 2025/4/13
 */
@Configuration
@EnableConfigurationProperties(OauthServerProperties.class)
public class SlardarOauthServerAutoConfiguration {
}
