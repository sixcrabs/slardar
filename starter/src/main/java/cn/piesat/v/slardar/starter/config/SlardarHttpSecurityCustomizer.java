package cn.piesat.v.slardar.starter.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/9/26
 */
@FunctionalInterface
public interface SlardarHttpSecurityCustomizer {

    /**
     * 自定义 HttpSecurity 行为
     * @param httpSecurity @link HttpSecurity
     */
    void customize(HttpSecurity httpSecurity);
}
