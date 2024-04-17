package cn.piesat.nj.slardar.starter.config;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.piesat.nj.misc.hutool.mini.StringUtil;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.authenticate.handler.SlardarAuthenticateHandlerFactory;
import cn.piesat.nj.slardar.starter.filter.*;
import cn.piesat.nj.slardar.starter.handler.SlardarAccessDeniedHandler;
import cn.piesat.nj.slardar.starter.handler.SlardarAuthenticateFailedHandler;
import cn.piesat.nj.slardar.starter.handler.SlardarAuthenticateSucceedHandler;
import cn.piesat.nj.slardar.starter.support.SecUtil;
import cn.piesat.nj.slardar.core.SlardarAuthority;
import cn.piesat.nj.slardar.core.SlardarIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.piesat.nj.slardar.starter.config.SlardarBeanConfiguration.STATIC_RES_MATCHERS;

/**
 * <p>
 * .
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/13
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
@AutoConfigureAfter({SlardarBeanConfiguration.class})
public class SlardarSecurityAdapter extends WebSecurityConfigurerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SlardarSecurityAdapter.class);


    @Autowired
    private SlardarAccessDeniedHandler accessDeniedHandler;

    @Autowired
    private SlardarAuthenticateFailedHandler authenticateFailedHandler;

    @Autowired
    private SlardarAuthenticateSucceedHandler authenticateSucceedHandler;

    @Autowired
    private SlardarAuthenticateHandlerFactory authenticateHandlerFactory;

    @Autowired
    private HttpFirewall httpFirewall;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    private final SlardarTokenRequiredFilter tokenRequiredFilter;

    private final SlardarBasicAuthFilter basicAuthFilter;

    private final SlardarCaptchaFilter captchaFilter;

    private final SlardarAuthenticatedRequestFilter authenticatedRequestFilter;

    private final SlardarMfaLoginFilter mfaLoginFilter;

    private final SlardarProperties properties;

    private final List<SlardarIgnoringCustomizer> ignoringCustomizerList;

    private final List<SlardarUrlRegistryCustomizer> urlRegistryCustomizerList;

    public SlardarSecurityAdapter(SlardarTokenRequiredFilter tokenRequiredFilter,
                                  ObjectProvider<SlardarBasicAuthFilter> basicAuthFilter,
                                  SlardarCaptchaFilter captchaFilter,
                                  SlardarAuthenticatedRequestFilter authenticatedRequestFilter,
                                  SlardarMfaLoginFilter mfaLoginFilter, SlardarProperties properties,
                                  ObjectProvider<List<SlardarIgnoringCustomizer>> ignoringCustomizerList,
                                  ObjectProvider<List<SlardarUrlRegistryCustomizer>> urlRegistryCustomizerProvider) {
        this.tokenRequiredFilter = tokenRequiredFilter;
        this.basicAuthFilter = basicAuthFilter.getIfAvailable();
        this.captchaFilter = captchaFilter;
        this.authenticatedRequestFilter = authenticatedRequestFilter;
        this.mfaLoginFilter = mfaLoginFilter;
        this.properties = properties;
        this.ignoringCustomizerList = ignoringCustomizerList.getIfAvailable();
        this.urlRegistryCustomizerList = urlRegistryCustomizerProvider.getIfAvailable();
    }

//    /**
//     * 配置校验的方法类
//     *
//     * @param auth
//     * @throws Exception
//     */
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.authenticationProvider(this.authenticationProvider);
//    }


    /**
     * 配置路径的访问权限
     *
     * @param httpSecurity
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {

        // 默认跨域: cors()
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry = httpSecurity.exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler).authenticationEntryPoint(authenticateFailedHandler)
                .and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .cors()
                .and()
                .authorizeRequests();

        registry.antMatchers(tokenRequiredFilter.getIgnoredUrls()).permitAll();
        // 应用扩展
        if (urlRegistryCustomizerList != null) {
            urlRegistryCustomizerList.forEach(registryCustomizer -> registryCustomizer.customize(registry));
        }
        // 注解扩展
        urlRegistryByAnnotation(registry);

        registry.antMatchers(HttpMethod.OPTIONS)
                .permitAll()
                .anyRequest()
                .authenticated();

        //禁用缓存
        httpSecurity.headers().cacheControl();
        // 禁用 iframe 策略
        httpSecurity.headers().frameOptions().disable();
        // 自定义 logout 处理
        httpSecurity.logout(logoutConfigurer -> logoutConfigurer.logoutUrl("/logout_spring"));
        // 设置filter
        httpSecurity.addFilterBefore(authenticatedRequestFilter, UsernamePasswordAuthenticationFilter.class);
        httpSecurity.addFilterBefore(tokenRequiredFilter, SlardarAuthenticatedRequestFilter.class);
        if (basicAuthFilter != null) {
            httpSecurity.addFilterBefore(basicAuthFilter, SlardarTokenRequiredFilter.class);
        }
        httpSecurity.addFilterBefore(loginProcessingFilter(properties, getManagerBean(), authenticateFailedHandler, authenticateSucceedHandler, authenticateHandlerFactory),
                SlardarTokenRequiredFilter.class);
        httpSecurity.addFilterBefore(captchaFilter, SlardarLoginProcessingFilter.class);
        httpSecurity.addFilterAfter(mfaLoginFilter, SlardarLoginProcessingFilter.class);
    }

    /**
     * 使用注解 定义接口方法权限
     *
     * @param registry
     */
    private void urlRegistryByAnnotation(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry) {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> methodEntry : handlerMethods.entrySet()) {
            HandlerMethod handlerMethod = methodEntry.getValue();
            if (handlerMethod.hasMethodAnnotation(SlardarAuthority.class)) {
                SlardarAuthority annotation = handlerMethod.getMethodAnnotation(SlardarAuthority.class);
                Set<String> patternValues = methodEntry.getKey().getPatternsCondition().getPatterns();
                Set<RequestMethod> methods = methodEntry.getKey().getMethodsCondition().getMethods();
                try {
                    if (CollectionUtils.isEmpty(methods)) {
                        // 没有指定method
                        setAuthorizedUrl(registry.antMatchers(patternValues.toArray(new String[0])), annotation.value());
                    } else {
                        for (RequestMethod method : methods) {
                            setAuthorizedUrl(registry.antMatchers(HttpMethod.resolve(method.name()), patternValues.toArray(new String[0])),
                                    annotation.value());
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage());
                }
            }
        }
    }

    private void setAuthorizedUrl(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl authorizedUrl,
                                  String expression) throws Exception {
        String method = ReUtil.getGroup1(SecUtil.AUTH_ANNOTATION_PATTERN, expression);
        if (StringUtil.isBlank(method)) {
            throw new SlardarException("expression [%] is invalid", expression);
        }
        String content = ReUtil.get(SecUtil.AUTH_ANNOTATION_PATTERN, expression, 2);
        if (content != null) {
            content = content.replaceAll("'", "");
        }
        if (StringUtil.isBlank(content)) {
            content = null;
        }
        // invoke
        try {
            if (StringUtil.isBlank(content)) {
                ReflectUtil.invoke(authorizedUrl, method);
            } else {
                // FIXME: hasAnyRole hasAnyAuthority 需要单独处理
                ReflectUtil.invoke(authorizedUrl, method, content.contains(",") ? content.split(",") : content);
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    /**
     * 集成方 可以实现 {@link SlardarIgnoringCustomizer} 定制需要忽略的 url pattern
     *
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
        web.ignoring().antMatchers(STATIC_RES_MATCHERS);
        web.ignoring().antMatchers(
                "swagger-ui.html",
                "**/swagger-ui.html",
                "/favicon.ico",
                "/**/*.css",
                "/**/*.js",
                "/**/*.png",
                "/**/*.gif",
                "/swagger-resources/**",
                "/v2/**",
                "/**/*.ttf"
        );
        web.ignoring().antMatchers("/v2/api-docs",
                "/swagger-resources/configuration/ui",
                "/swagger-resources",
                "/swagger-resources/configuration/security",
                "/swagger-ui.html",
                "/doc.html"
        );
        /**
         * 使用 {@link SlardarIgnore} 注解的忽略
         */
        ignoreByAnnotation(web.ignoring());
        // 应用定制扩展
        if (this.ignoringCustomizerList != null) {
            for (SlardarIgnoringCustomizer ignoringCustomizer : ignoringCustomizerList) {
                ignoringCustomizer.customize(web.ignoring());
            }
        }
        //url中允许使用双斜杠
        web.httpFirewall(httpFirewall);
    }

    private void ignoreByAnnotation(WebSecurity.IgnoredRequestConfigurer ignoredRequestConfigurer) {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> methodEntry : handlerMethods.entrySet()) {
            HandlerMethod handlerMethod = methodEntry.getValue();
            if (handlerMethod.hasMethodAnnotation(SlardarIgnore.class)) {
                Set<String> patternValues = methodEntry.getKey().getPatternsCondition().getPatterns();
                Set<RequestMethod> methods = methodEntry.getKey().getMethodsCondition().getMethods();
                if (CollectionUtils.isEmpty(methods)) {
                    // 没有指定method
                    ignoredRequestConfigurer.antMatchers(patternValues.toArray(new String[0]));
                    patternValues.forEach(pattern -> tokenRequiredFilter.addIgnoreUrlPattern(pattern, null));
                } else {
                    for (RequestMethod method : methods) {
                        ignoredRequestConfigurer.antMatchers(HttpMethod.resolve(method.name()), patternValues.toArray(new String[0]));
                        patternValues.forEach(pattern -> tokenRequiredFilter.addIgnoreUrlPattern(pattern, method.name()));
                    }
                }
            }
        }
    }


    @Bean
    public AuthenticationManager getManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    /**
     * 登录请求
     *
     * @param properties
     * @param authenticationManager
     * @param failureHandler
     * @param successHandler
     * @param authenticateHandlerFactory
     * @return
     */
    @Bean
    public SlardarLoginProcessingFilter loginProcessingFilter(SlardarProperties properties,
                                                              AuthenticationManager authenticationManager,
                                                              AuthenticationFailureHandler failureHandler,
                                                              AuthenticationSuccessHandler successHandler,
                                                              SlardarAuthenticateHandlerFactory authenticateHandlerFactory) {
        return new SlardarLoginProcessingFilter(properties, authenticationManager, failureHandler, successHandler, authenticateHandlerFactory);
    }

}
