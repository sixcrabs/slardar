package io.github.sixcrabs.slardar.starter.config.customizer;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * <p>
 * 定制 http security
 * </p>
 *
 * @author Alex
 * @since 2025/9/26
 */
@FunctionalInterface
public interface SlardarHttpSecurityCustomizer {

    /**
     * 自定义 HttpSecurity 行为
     * @param httpSecurity {@linkplain HttpSecurity}
     */
    void customize(HttpSecurity httpSecurity);
}