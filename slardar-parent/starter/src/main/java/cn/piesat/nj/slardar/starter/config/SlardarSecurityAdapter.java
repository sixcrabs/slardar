package cn.piesat.nj.slardar.starter.config;

import cn.piesat.nj.slardar.starter.SlardarAuthenticationProvider;
import cn.piesat.nj.slardar.starter.filter.*;
import cn.piesat.nj.slardar.starter.handler.SlardarAccessDeniedHandler;
import cn.piesat.nj.slardar.starter.handler.SlardarAuthenticateFailedHandler;
import cn.piesat.nj.slardar.starter.handler.SlardarAuthenticateSucceedHandler;
import cn.piesat.nj.slardar.starter.handler.authentication.AuthenticationRequestHandlerFactory;
import cn.piesat.nj.slardar.starter.support.SlardarIgnore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
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


    @Autowired
    private SlardarAccessDeniedHandler accessDeniedHandler;

    @Autowired
    private SlardarAuthenticateFailedHandler authenticateFailedHandler;

    @Autowired
    private SlardarAuthenticateSucceedHandler authenticateSucceedHandler;

    @Autowired
    private AuthenticationRequestHandlerFactory authenticationRequestHandlerFactory;

    @Autowired
    private HttpFirewall httpFirewall;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    private final SlardarTokenRequiredFilter tokenRequiredFilter;

    private final SlardarCaptchaFilter captchaFilter;

    private final SlardarAuthenticatedRequestFilter authenticatedRequestFilter;

    private final SlardarProperties properties;

    private final SlardarAuthenticationProvider authenticationProvider;

    private final List<SlardarIgnoringCustomizer> ignoringCustomizerList;

    public SlardarSecurityAdapter(SlardarTokenRequiredFilter tokenRequiredFilter,
                                  SlardarCaptchaFilter captchaFilter,
                                  SlardarAuthenticatedRequestFilter authenticatedRequestFilter,
                                  SlardarProperties properties, SlardarAuthenticationProvider slardarAuthenticationProvider,
                                  ObjectProvider<List<SlardarIgnoringCustomizer>> ignoringCustomizerList) {
        this.tokenRequiredFilter = tokenRequiredFilter;
        this.captchaFilter = captchaFilter;
        this.authenticatedRequestFilter = authenticatedRequestFilter;
        this.properties = properties;
        this.authenticationProvider = slardarAuthenticationProvider;
        this.ignoringCustomizerList = ignoringCustomizerList.getIfAvailable();
    }

    /**
     * 配置校验的方法类
     *
     * @param auth
     * @throws Exception
     * @see cn.piesat.nj.slardar.starter.provider.SlardarAuthenticationProviderImpl
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(this.authenticationProvider);
    }


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
        httpSecurity.addFilterBefore(loginProcessingFilter(properties, getManagerBean(), authenticateFailedHandler, authenticateSucceedHandler, authenticationRequestHandlerFactory),
                SlardarTokenRequiredFilter.class);
        httpSecurity.addFilterBefore(captchaFilter, SlardarLoginProcessingFilter.class);


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
         * 使用 {@link cn.piesat.nj.slardar.starter.support.SlardarIgnore} 注解的忽略
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
                } else {
                    for (RequestMethod method : methods) {
                        ignoredRequestConfigurer.antMatchers(HttpMethod.resolve(method.name()), patternValues.toArray(new String[0]));
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
     * @param authenticationRequestHandlerFactory
     * @return
     */
    @Bean
    public SlardarLoginProcessingFilter loginProcessingFilter(SlardarProperties properties,
                                                              AuthenticationManager authenticationManager,
                                                              AuthenticationFailureHandler failureHandler,
                                                              AuthenticationSuccessHandler successHandler,
                                                              AuthenticationRequestHandlerFactory authenticationRequestHandlerFactory) {
        return new SlardarLoginProcessingFilter(properties, authenticationManager, failureHandler, successHandler, authenticationRequestHandlerFactory);
    }

}
