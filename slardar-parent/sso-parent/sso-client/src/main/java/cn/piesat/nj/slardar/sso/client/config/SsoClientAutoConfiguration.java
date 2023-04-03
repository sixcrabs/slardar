package cn.piesat.nj.slardar.sso.client.config;

import cn.piesat.nj.cloud.mrc.EnableMrClients;
import cn.piesat.nj.slardar.sso.client.config.client.SsoServerClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableMrClients(clients = SsoServerClient.class)
@EnableConfigurationProperties(SsoClientProperties.class)
public class SsoClientAutoConfiguration {
}
