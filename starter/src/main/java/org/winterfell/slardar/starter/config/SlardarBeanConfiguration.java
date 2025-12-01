package org.winterfell.slardar.starter.config;

import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.winterfell.misc.hutool.mini.ArrayUtil;
import org.winterfell.misc.keystore.KeyStoreProperties;
import org.winterfell.misc.keystore.RedisKeyStoreImpl;
import org.winterfell.misc.keystore.SimpleKeyStore;
import org.winterfell.misc.keystore.SimpleKeyStoreFactory;
import org.winterfell.slardar.core.SlardarContext;
import org.winterfell.slardar.spi.SlardarSpiFactory;
import org.winterfell.slardar.starter.*;
import org.winterfell.slardar.starter.authenticate.SlardarAuthenticateService;
import org.winterfell.slardar.starter.authenticate.SlardarUserDetailsServiceImpl;
import org.winterfell.slardar.starter.authenticate.handler.SlardarAuthenticateHandlerFactory;
import org.winterfell.slardar.starter.authenticate.mfa.SlardarMfaAuthService;
import org.winterfell.slardar.starter.filter.SlardarAuthenticatedRequestFilter;
import org.winterfell.slardar.starter.filter.SlardarCaptchaFilter;
import org.winterfell.slardar.starter.filter.SlardarLoginFilter;
import org.winterfell.slardar.starter.filter.SlardarMfaFilter;
import org.winterfell.slardar.starter.filter.request.SlardarApiSignatureFilter;
import org.winterfell.slardar.starter.filter.request.SlardarBasicAuthFilter;
import org.winterfell.slardar.starter.filter.request.SlardarTokenRequiredFilter;
import org.winterfell.slardar.starter.handler.SlardarAccessDeniedHandler;
import org.winterfell.slardar.starter.handler.SlardarAuthenticateFailedHandler;
import org.winterfell.slardar.starter.handler.SlardarAuthenticateSucceedHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
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
@AutoConfiguration
@EnableConfigurationProperties({SlardarProperties.class})
@ComponentScan(basePackages = {"org.winterfell"})
public class SlardarBeanConfiguration {


    /**
     * 静态资源 不拦截
     */
    public static final String[] STATIC_RES_MATCHERS = new String[]{
            "/",
            "/*.html",
            "/**/*.html",
            "/v2/api-docs/**",
            "/v3/api-docs/**",
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
            "/**/*.svg",
            "/favicon.ico",
            "/manifest.json",
            "/swagger-resources/**",
            "/v2/api-docs"};

    /**
     * 注入事件管理
     *
     * @return
     */
    @Bean
    public SlardarEventManager eventManager() {
        return new SlardarEventManager();
    }

    @Bean(initMethod = "initialize")
    public SlardarContext slardarSpiContext(SlardarProperties properties) {
        return new SpringSlardarContextImpl(properties);
    }

    @Bean
    public SlardarSpiFactory slardarSpiFactory(SlardarContext spiContext) {
        return new SpringSlardarSpiFactory(spiContext);
    }

    @Bean
    public SimpleKeyStore simpleKeyStore(SlardarProperties properties) {
        return SimpleKeyStoreFactory.getInstance().getKeyStore(properties.getKeyStore());
    }

    /**
     * 注入认证处理 service
     *
     * @param spiFactory
     * @param properties
     * @return
     */
    @Bean
    public SlardarAuthenticateService authenticateService(SlardarSpiFactory spiFactory, SlardarProperties properties) {
        return new SlardarAuthenticateService(properties, spiFactory);
    }

    /**
     * 注入 认证 handler factory
     *
     * @return
     */
    @Bean
    public SlardarAuthenticateHandlerFactory authenticateHandlerFactory(SlardarContext context) {
        return new SlardarAuthenticateHandlerFactory(context);
    }


    /**
     * 注入 认证失败 handler
     *
     * @return
     */
    @Bean
    public SlardarAuthenticateFailedHandler authenticationFailureHandler(SlardarContext context, SlardarAuthenticateService authenticateService) {
        return new SlardarAuthenticateFailedHandler(context, authenticateService);
    }

    /**
     * 注入 认证成功 handler
     *
     * @return
     */
    @Bean
    public SlardarAuthenticateSucceedHandler authenticateSucceedHandler(SlardarAuthenticateService tokenService,
                                                                        SlardarContext context) {
        return new SlardarAuthenticateSucceedHandler(tokenService, context);
    }

    /**
     * 注入拒绝访问 handler
     *
     * @return
     */
    @Bean
    public SlardarAccessDeniedHandler accessDeniedHandler(SlardarAuthenticateService authenticateService) {
        return new SlardarAccessDeniedHandler(authenticateService);
    }

    /**
     * 处理 MFA 认证
     *
     * @param spiFactory
     * @param slardarProperties
     * @return
     */
    @Bean
    public SlardarMfaAuthService slardarMfaAuthService(SlardarSpiFactory spiFactory, SlardarProperties slardarProperties,
                                                       SimpleKeyStore simpleKeyStore) {
        return new SlardarMfaAuthService(spiFactory, slardarProperties, simpleKeyStore);
    }

    /**
     * 注入用户详情获取 service
     *
     * @param context
     * @return
     */
    @Bean
    public SlardarUserDetailsServiceImpl userDetailsService(SlardarContext context) {
        return new SlardarUserDetailsServiceImpl(context);
    }

    /**
     * MFA 登录过滤器
     *
     * @return
     */
    @Bean
    @DependsOn("slardarMfaAuthService")
    public SlardarMfaFilter slardarMfaLoginFilter(SlardarMfaAuthService mfaAuthService,
                                                  AuthenticationFailureHandler failureHandler,
                                                  AuthenticationSuccessHandler successHandler) {
        return new SlardarMfaFilter(mfaAuthService, failureHandler, successHandler);
    }

    @Bean
    @ConditionalOnProperty(name = "slardar.basic.enable")
    @DependsOn("authenticateService")
    public SlardarBasicAuthFilter basicAuthFilter(SlardarProperties properties, SlardarContext spiContext) {
        return new SlardarBasicAuthFilter(properties.getBasic().getFilterUrls(), spiContext);
    }

    @Bean
    @ConditionalOnProperty(name = "slardar.signature.enable")
    public SlardarApiSignatureFilter apiSignatureFilter(SlardarProperties properties, SlardarContext spiContext, SlardarSpiFactory spiFactory) {
        return new SlardarApiSignatureFilter(spiContext, spiFactory, properties);
    }

    /**
     * 注入 请求过滤器 用于过滤所有请求进行token验证
     *
     * @param properties
     * @return
     */
    @Bean
    @DependsOn("authenticateService")
    public SlardarTokenRequiredFilter requestFilter(SlardarProperties properties) {
        // 忽略的url 包含配置的参数以及静态资源、swagger context
        String[] ignoresFromConfig = properties.getIgnores();
        // 将 basic 认证的url加入到 ignore 数组中
        if (properties.getBasic().isEnable() && properties.getBasic().getFilterUrls().length > 0) {
            ignoresFromConfig = ArrayUtil.append(ignoresFromConfig, properties.getBasic().getFilterUrls());
        }
        String[] ignores = Arrays.copyOf(STATIC_RES_MATCHERS, STATIC_RES_MATCHERS.length + ignoresFromConfig.length);
        System.arraycopy(ignoresFromConfig, 0, ignores, STATIC_RES_MATCHERS.length, ignoresFromConfig.length);
        ignores = ArrayUtil.append(ignores, "/oauth2/**", "/error", "/mfa-login",
                properties.getLogin().getUrl(), properties.getCaptcha().getUrl());
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
    @DependsOn("authenticateService")
    public SlardarAuthenticatedRequestFilter userDetailsProcessingFilter(SlardarProperties properties, SlardarContext context) {
        return new SlardarAuthenticatedRequestFilter(properties, context);
    }

    /**
     * 验证码过滤器
     *
     * @return
     */
    @Bean
    public SlardarCaptchaFilter slardarCaptchaFilter(SlardarProperties properties) {
        return new SlardarCaptchaFilter(properties);
    }

    /**
     * 登录请求
     *
     * @param properties
     * @param failureHandler
     * @param successHandler
     * @param authenticateHandlerFactory
     * @return
     */
    @Bean
    public SlardarLoginFilter loginProcessingFilter(SlardarProperties properties,
                                                    AuthenticationFailureHandler failureHandler,
                                                    AuthenticationSuccessHandler successHandler,
                                                    SlardarAuthenticateHandlerFactory authenticateHandlerFactory) {
        return new SlardarLoginFilter(properties, failureHandler, successHandler, authenticateHandlerFactory);
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

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(HttpFirewall httpFirewall) {
        return (web) -> {
            //url中允许使用双斜杠
            web.httpFirewall(httpFirewall);
        };
    }
}