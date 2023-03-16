package cn.piesat.nj.slardar.starter.config;

import cn.hutool.core.util.ArrayUtil;
import cn.piesat.nj.skv.core.KvStore;
import cn.piesat.nj.skv.starter.config.KvAutoConfiguration;
import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.SlardarTokenService;
import cn.piesat.nj.slardar.starter.SlardarUserDetailsService;
import cn.piesat.nj.slardar.starter.filter.SlardarCaptchaFilter;
import cn.piesat.nj.slardar.starter.filter.SlardarRequestFilter;
import cn.piesat.nj.slardar.starter.filter.SlardarUserDetailsProcessingFilter;
import cn.piesat.nj.slardar.starter.handler.SlardarAccessDeniedHandler;
import cn.piesat.nj.slardar.starter.handler.SlardarAuthenticateFailedHandler;
import cn.piesat.nj.slardar.starter.handler.SlardarAuthenticateSucceedHandler;
import cn.piesat.nj.slardar.starter.handler.authentication.AuthenticationRequestHandlerFactory;
import cn.piesat.nj.slardar.starter.provider.SlardarAuthenticationProviderImpl;
import io.lettuce.core.RedisClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import java.util.Arrays;

/**
 * <p>
 * 注入 需要的各类 bean
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/13
 */
@Configuration
@EnableConfigurationProperties(SlardarProperties.class)
@ComponentScan(basePackages = {"cn.piesat"})
@AutoConfigureAfter(KvAutoConfiguration.class)
public class SlardarBeanConfiguration {


    /**
     * 静态资源 不拦截
     */
    public static final String[] STATIC_RES_MATCHERS = new String[]{
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

    /**
     * 注入 认证失败 handler
     * @return
     */
    @Bean
    public SlardarAuthenticateFailedHandler authenticationFailureHandler() {
        return new SlardarAuthenticateFailedHandler();
    }

    /**
     * 注入 认证成功 handler
     * @param properties
     * @return
     */
    @Bean
    public SlardarAuthenticateSucceedHandler authenticateSucceedHandler(SlardarProperties properties,
                                                                        SlardarTokenService tokenService, SlardarContext context) {
        return new SlardarAuthenticateSucceedHandler(properties, tokenService, context);
    }

    /**
     * 注入拒绝访问 handler
     * @return
     */
    @Bean
    public SlardarAccessDeniedHandler accessDeniedHandler() {
        return new SlardarAccessDeniedHandler();
    }

    /**
     * 注入 认证请求handler factory
     * @return
     */
    @Bean
    public AuthenticationRequestHandlerFactory authenticationRequestHandlerFactory() {
        return new AuthenticationRequestHandlerFactory();
    }

    /**
     * 认证的默认实现类
     * @param passwordEncoder
     * @return
     */
    @Bean
    public SlardarAuthenticationProviderImpl authenticationProvider(PasswordEncoder passwordEncoder) {
        return new SlardarAuthenticationProviderImpl(passwordEncoder);
    }

    /**
     * 注入上下文 context
     * @return
     */
    @Bean
    public SlardarContext slardarContext() {
        return new SlardarContext();
    }

    /**
     * 注入用户详情获取 service
     * @param context
     * @return
     */
    @Bean
    public SlardarUserDetailsService userDetailsService(SlardarContext context) {
        return new SlardarUserDetailsService(context);
    }

    /**
     * 注入 token 处理 service
     * @param context
     * @param properties
     * @param kvStore
     * @param redisClient
     * @return
     */
    @Bean
    public SlardarTokenService tokenService(SlardarContext context, SlardarProperties properties, KvStore kvStore, RedisClient redisClient) {
        return new SlardarTokenService(properties, context, kvStore, redisClient);
    }

    /**
     * 注入 请求过滤器 用于过滤所有请求进行token验证
     * @param properties
     * @return
     */
    @Bean
    public SlardarRequestFilter requestFilter(SlardarProperties properties) {
        // 忽略的url 包含配置的参数以及静态资源、swagger context
        String[] ignoresFromConfig = properties.getIgnores();
        String[] ignores = Arrays.copyOf(STATIC_RES_MATCHERS, STATIC_RES_MATCHERS.length + ignoresFromConfig.length);
        System.arraycopy(ignoresFromConfig, 0, ignores, STATIC_RES_MATCHERS.length, ignoresFromConfig.length);
        ignores = ArrayUtil.append(ignores, "/oauth2/**", properties.getLogin().getUrl());
        return new SlardarRequestFilter(ignores);
    }

    /**
     * 通过 token 获取用户详情
     * @param properties
     * @param context
     * @return
     */
    @Bean
    public SlardarUserDetailsProcessingFilter userDetailsProcessingFilter(SlardarProperties properties, SlardarContext context) {
        return new SlardarUserDetailsProcessingFilter(properties, context);
    }

    @Bean
    public SlardarCaptchaFilter slardarCaptchaFilter() {
        return new SlardarCaptchaFilter();
    }



    /**
     * 配置地址栏不能识别 // 的情况
     *
     * @return
     */
    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        //此处可添加别的规则,目前只设置 允许双 //
        firewall.setAllowUrlEncodedDoubleSlash(true);
        return firewall;
    }
}
