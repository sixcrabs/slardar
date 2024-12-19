package cn.piesat.v.slardar.example.config;

import cn.piesat.v.slardar.starter.config.SlardarUrlRegistryCustomizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.stereotype.Component;

/**
 * <p>
 * .
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/26
 */
@Component
public class MyUrlRegistryCustomizerImpl implements SlardarUrlRegistryCustomizer {

    /**
     * 配置自定义受限 url 信息
     *
     * @param registry
     */
    @Override
    public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        // /admin 的url只能被 admin 角色访问
        registry.antMatchers("/api/admin/**").hasAnyRole("ADMIN", "SYS_ADMIN");
    }
}
