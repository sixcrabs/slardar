package io.github.sixcrabs.slardar.starter.config.customizer;


import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * <p>
 * 自定义url 认证/授权拦截注册
 * eg:
 * - hasRole('ADMIN')
 * - permitAll()
 * <p>
 * 用法:
 * <code>
 * `@Component`
 * public class MyRegistryCustomizerImpl implements SlardarUrlRegistryCustomizer {
 *
 *  `@Override`
 *  public void customize(AuthorizeHttpRequestsConfigurer&lt;HttpSecurity&gt;.AuthorizationManagerRequestMatcherRegistry registry){
 *          // /admin 的url只能被 admin 角色访问
 *          registry.antMatchers("/api/admin/**").hasAnyRole("ADMIN","SYS_ADMIN");
 *      }
 * }
 * </code>
 *
 * @author alex
 * @version v1.0 2023/3/26
 */
@FunctionalInterface
public interface SlardarUrlRegistryCustomizer {

    /**
     * 配置自定义受限 url 信息
     *
     * @param registry
     */
    void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry);
}