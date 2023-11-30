package cn.piesat.nj.slardar.starter.config;

import cn.hutool.core.util.ArrayUtil;
import cn.piesat.nj.skv.core.KvStore;
import cn.piesat.nj.skv.starter.config.KvAutoConfiguration;
import cn.piesat.nj.slardar.spi.SlardarSpiContext;
import cn.piesat.nj.slardar.spi.SlardarSpiFactory;
import cn.piesat.nj.slardar.starter.*;
import cn.piesat.nj.slardar.starter.authenticate.handler.SlardarAuthenticateHandlerFactory;
import cn.piesat.nj.slardar.starter.authenticate.mfa.SlardarMfaAuthService;
import cn.piesat.nj.slardar.starter.filter.SlardarCaptchaFilter;
import cn.piesat.nj.slardar.starter.filter.SlardarMfaLoginFilter;
import cn.piesat.nj.slardar.starter.filter.SlardarTokenRequiredFilter;
import cn.piesat.nj.slardar.starter.filter.SlardarAuthenticatedRequestFilter;
import cn.piesat.nj.slardar.starter.handler.SlardarAccessDeniedHandler;
import cn.piesat.nj.slardar.starter.handler.SlardarAuthenticateFailedHandler;
import cn.piesat.nj.slardar.starter.handler.SlardarAuthenticateSucceedHandler;
import io.lettuce.core.RedisClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
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

    /**
     * 注入时间管理
     *
     * @return
     */
    @Bean
    public SlardarEventManager eventManager() {
        return new SlardarEventManager();
    }

    /**
     * 注入 认证失败 handler
     *
     * @return
     */
    @Bean
    public SlardarAuthenticateFailedHandler authenticationFailureHandler(SlardarSpiContext context) {
        return new SlardarAuthenticateFailedHandler(context);
    }

    /**
     * 注入 认证成功 handler
     *
     * @param properties
     * @return
     */
    @Bean
    public SlardarAuthenticateSucceedHandler authenticateSucceedHandler(SlardarProperties properties,
                                                                        SlardarTokenService tokenService,
                                                                        SlardarSpiContext context) {
        return new SlardarAuthenticateSucceedHandler(properties, tokenService, context);
    }

    /**
     * 注入拒绝访问 handler
     *
     * @return
     */
    @Bean
    public SlardarAccessDeniedHandler accessDeniedHandler() {
        return new SlardarAccessDeniedHandler();
    }


    /**
     * 处理 MFA 认证
     * @param spiFactory
     * @param slardarProperties
     * @param kvStore
     * @return
     */
    @Bean
    public SlardarMfaAuthService slardarMfaAuthService(SlardarSpiFactory spiFactory, SlardarProperties slardarProperties,
                                                       KvStore kvStore) {
        return new SlardarMfaAuthService(spiFactory, slardarProperties, kvStore);
    }

    /**
     * 注入 认证 handler factory
     *
     * @return
     */
    @Bean
    public SlardarAuthenticateHandlerFactory authenticateHandlerFactory(SlardarSpiContext context) {
        return new SlardarAuthenticateHandlerFactory(context);
    }

    /**
     * MFA 登录过滤器
     *
     * @return
     */
    @Bean
    public SlardarMfaLoginFilter slardarMfaLoginFilter(SlardarMfaAuthService mfaAuthService,
                                                       AuthenticationFailureHandler failureHandler,
                                                       AuthenticationSuccessHandler successHandler) {
        return new SlardarMfaLoginFilter(mfaAuthService, failureHandler, successHandler);
    }

    @Bean
    public SlardarSpiContext slardarSpiContext() {
        return new SpringSlardarContextImpl();
    }


    @Bean
    public SlardarSpiFactory slardarSpiFactory(SlardarSpiContext spiContext) {
        return new SpringSlardarSpiFactory(spiContext);
    }

    /**
     * 注入用户详情获取 service
     *
     * @param context
     * @return
     */
    @Bean
    public SlardarUserDetailsServiceImpl userDetailsService(SlardarSpiContext context) {
        return new SlardarUserDetailsServiceImpl(context);
    }

    /**
     * 注入 token 处理 service
     *
     * @param context
     * @param properties
     * @param kvStore
     * @param redisClient
     * @return
     */
    @Bean
    public SlardarTokenService tokenService(SlardarSpiContext context,SlardarSpiFactory spiFactory,
                                            SlardarProperties properties, KvStore kvStore, RedisClient redisClient) {
        return new SlardarTokenService(properties, spiFactory, context, kvStore, redisClient);
    }

    /**
     * 注入 请求过滤器 用于过滤所有请求进行token验证
     *
     * @param properties
     * @return
     */
    @Bean
    public SlardarTokenRequiredFilter requestFilter(SlardarProperties properties) {
        // 忽略的url 包含配置的参数以及静态资源、swagger context
        String[] ignoresFromConfig = properties.getIgnores();
        String[] ignores = Arrays.copyOf(STATIC_RES_MATCHERS, STATIC_RES_MATCHERS.length + ignoresFromConfig.length);
        System.arraycopy(ignoresFromConfig, 0, ignores, STATIC_RES_MATCHERS.length, ignoresFromConfig.length);
        ignores = ArrayUtil.append(ignores, "/oauth2/**",
                properties.getLogin().getUrl(), "/error", "/mfa-login");
        return new SlardarTokenRequiredFilter(ignores);
    }

    /**
     * 通过 token 获取用户详情
     *
     * @param properties
     * @param context
     * @return
     */
    @Bean
    public SlardarAuthenticatedRequestFilter userDetailsProcessingFilter(SlardarProperties properties, SlardarSpiContext context) {
        return new SlardarAuthenticatedRequestFilter(properties, context);
    }

    /**
     * 验证码过滤器
     *
     * @return
     */
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
