package cn.piesat.nj.slardar.starter.config;

import cn.hutool.core.util.ArrayUtil;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

/**
 * <p>
 * .
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/13
 */
@Configuration
@EnableConfigurationProperties(SlardarProperties.class)
@ComponentScan(basePackages = {"cn.piesat"})
public class SlardarConfiguration {


    /**
     * 静态资源 不拦截
     */
    private static final String[] STATIC_RES_MATCHERS = new String[]{
            "/",
            "/*.html",
            "/v2/api-docs/**",
            "/doc.html",
            "/image/**",
            "/css/**",
            "/js/**",
            "/webjars/**",
            "/font/**",
            "/**/*.css",
            "/**/*.js",
            "/**/*.png",
            "/**/*.jpg",
            "/**/*.jpeg",
            "/favicon.ico",
            "/manifest.json",
            "/*.html",
            "/**/*.html",
            "/swagger-resources/**",
            "/v2/api-docs"};


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthxAuthenticateFailedHandler authenticationFailureHandler() {
        return new AuthxAuthenticateFailedHandler();
    }

    @Bean
    public AuthxAuthenticateSucceedHandler authenticateSucceedHandler(SlardarProperties properties) {
        return new AuthxAuthenticateSucceedHandler(properties);
    }

    @Bean
    public AuthxAccessDeniedHandler accessDeniedHandler() {
        return new AuthxAccessDeniedHandler();
    }

    @Bean
    public AuthxAuthenticationRequestHandlerFactory authenticationRequestHandlerFactory() {
        return new AuthxAuthenticationRequestHandlerFactory();
    }

    @Bean
    public AuthxDefaultAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        return new AuthxDefaultAuthenticationProvider(passwordEncoder);
    }

    @Bean
    public AuthxUserDetailsService userDetailsService() {
        return new AuthxUserDetailsService();
    }

    @Bean
    public AuthxTokenFilter authxTokenFilter(SlardarProperties properties) {
        // 忽略的url 包含配置的参数以及静态资源、swagger context
        String[] ignoresFromConfig = properties.getIgnores();
        String[] ignores = Arrays.copyOf(STATIC_RES_MATCHERS, STATIC_RES_MATCHERS.length + ignoresFromConfig.length);
        System.arraycopy(ignoresFromConfig, 0, ignores, STATIC_RES_MATCHERS.length, ignoresFromConfig.length);
        ignores = ArrayUtil.append(ignores, "/oauth2/**", properties.getLogin().getUrl());
        return new AuthxTokenFilter(ignores);
    }
}
