package cn.piesat.v.slardar.starter.config;

import cn.piesat.v.misc.hutool.mini.ReUtil;
import cn.piesat.v.misc.hutool.mini.ReflectUtil;
import cn.piesat.v.misc.hutool.mini.StringUtil;
import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.core.annotation.SlardarAuthority;
import cn.piesat.v.slardar.core.annotation.SlardarIgnore;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.starter.authenticate.handler.SlardarAuthenticateHandlerFactory;
import cn.piesat.v.slardar.starter.filter.SlardarAuthenticatedRequestFilter;
import cn.piesat.v.slardar.starter.filter.SlardarCaptchaFilter;
import cn.piesat.v.slardar.starter.filter.SlardarLoginFilter;
import cn.piesat.v.slardar.starter.filter.SlardarMfaFilter;
import cn.piesat.v.slardar.starter.filter.request.SlardarBasicAuthFilter;
import cn.piesat.v.slardar.starter.filter.request.SlardarTokenRequiredFilter;
import cn.piesat.v.slardar.starter.handler.SlardarAccessDeniedHandler;
import cn.piesat.v.slardar.starter.handler.SlardarAuthenticateFailedHandler;
import cn.piesat.v.slardar.starter.handler.SlardarAuthenticateSucceedHandler;
import cn.piesat.v.slardar.starter.support.SecUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * spring security 定制
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/12/18
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
@AutoConfigureAfter({SlardarBeanConfiguration.class})
public class SlardarSecurityConfiguration {


    private static final Logger logger = LoggerFactory.getLogger(SlardarSecurityConfiguration.class);


    @Autowired
    private SlardarAccessDeniedHandler accessDeniedHandler;

    @Autowired
    private SlardarAuthenticateFailedHandler authenticateFailedHandler;

    @Autowired
    private HttpFirewall httpFirewall;

    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    private final SlardarTokenRequiredFilter tokenRequiredFilter;

    private final SlardarBasicAuthFilter basicAuthFilter;

    private final SlardarCaptchaFilter captchaFilter;

    private final SlardarLoginFilter loginFilter;

    private final SlardarAuthenticatedRequestFilter authenticatedRequestFilter;

    private final SlardarMfaFilter mfaLoginFilter;

    private final SlardarProperties properties;

    private final List<SlardarIgnoringCustomizer> ignoringCustomizerList;

    private final List<SlardarUrlRegistryCustomizer> urlRegistryCustomizerList;

    private final List<SlardarHttpSecurityCustomizer> httpSecurityCustomizers;

    public SlardarSecurityConfiguration(SlardarTokenRequiredFilter tokenRequiredFilter,
                                        SlardarSpiContext slardarSpiContext,
                                        SlardarCaptchaFilter captchaFilter, SlardarLoginFilter loginFilter,
                                        SlardarAuthenticatedRequestFilter authenticatedRequestFilter,
                                        SlardarMfaFilter mfaLoginFilter, SlardarProperties properties,
                                        List<SlardarIgnoringCustomizer> ignoringCustomizerList,
                                        List<SlardarUrlRegistryCustomizer> urlRegistryCustomizerList,
                                        List<SlardarHttpSecurityCustomizer> httpSecurityCustomizers) {
        this.tokenRequiredFilter = tokenRequiredFilter;
        this.basicAuthFilter = slardarSpiContext.getBeanIfAvailable(SlardarBasicAuthFilter.class);
        this.captchaFilter = captchaFilter;
        this.loginFilter = loginFilter;
        this.authenticatedRequestFilter = authenticatedRequestFilter;
        this.mfaLoginFilter = mfaLoginFilter;
        this.properties = properties;
        this.ignoringCustomizerList = ignoringCustomizerList;
        this.urlRegistryCustomizerList = urlRegistryCustomizerList;
        this.httpSecurityCustomizers = httpSecurityCustomizers;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry = httpSecurity.exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(authenticateFailedHandler)
                .and().csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().cors()
                .and()
                .authorizeHttpRequests();

        // 使用 {@link SlardarIgnore} 注解的忽略
        permitAllByAnno(registry);
        // 应用定制扩展ignore url
        if (this.ignoringCustomizerList != null) {
            List<String> patterns = new ArrayList<>(1);
            for (SlardarIgnoringCustomizer ignoringCustomizer : ignoringCustomizerList) {
                ignoringCustomizer.customize(patterns);
            }
            registry.antMatchers(patterns.toArray(new String[0])).permitAll();
            patterns.forEach(p -> tokenRequiredFilter.addIgnoreUrlPattern(p, null));
        }
        // 读取所有ignore的url并 permitAll
        registry.antMatchers(tokenRequiredFilter.getIgnoredUrls()).permitAll();

        // 应用扩展
        if (urlRegistryCustomizerList != null) {
            urlRegistryCustomizerList.forEach(registryCustomizer -> registryCustomizer.customize(registry));
        }
        // 注解
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
        httpSecurity.addFilterBefore(loginFilter, SlardarTokenRequiredFilter.class);
        httpSecurity.addFilterBefore(captchaFilter, SlardarLoginFilter.class);
        httpSecurity.addFilterAfter(mfaLoginFilter, SlardarLoginFilter.class);
        if (httpSecurityCustomizers != null) {
            httpSecurityCustomizers.forEach(customizer -> customizer.customize(httpSecurity));
        }
        return httpSecurity.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> {
            //url中允许使用双斜杠
            web.httpFirewall(httpFirewall);
        };
    }

    private void permitAllByAnno(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> methodEntry : handlerMethods.entrySet()) {
            HandlerMethod handlerMethod = methodEntry.getValue();
            if (handlerMethod.hasMethodAnnotation(SlardarIgnore.class)) {
                Set<String> patternValues = methodEntry.getKey().getPatternsCondition() != null ? methodEntry.getKey().getPatternsCondition().getPatterns() : methodEntry.getKey().getPatternValues();
                Set<RequestMethod> methods = methodEntry.getKey().getMethodsCondition().getMethods();
                if (CollectionUtils.isEmpty(methods)) {
                    registry.antMatchers(patternValues.toArray(new String[0])).permitAll();
                    patternValues.forEach(pattern -> tokenRequiredFilter.addIgnoreUrlPattern(pattern, null));
                } else {
                    for (RequestMethod method : methods) {
                        registry.antMatchers(HttpMethod.resolve(method.name()), patternValues.toArray(new String[0])).permitAll();
                        patternValues.forEach(pattern -> tokenRequiredFilter.addIgnoreUrlPattern(pattern, method.name()));
                    }
                }
            }
        }
    }

    private void setAuthorizedUrl(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl authorizedUrl,
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
     * 使用注解 定义接口方法权限
     *
     * @param registry
     */
    private void urlRegistryByAnnotation(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> methodEntry : handlerMethods.entrySet()) {
            HandlerMethod handlerMethod = methodEntry.getValue();
            if (handlerMethod.hasMethodAnnotation(SlardarAuthority.class)) {
                SlardarAuthority annotation = handlerMethod.getMethodAnnotation(SlardarAuthority.class);
                Set<String> patternValues = methodEntry.getKey().getPatternsCondition() != null ? methodEntry.getKey().getPatternsCondition().getPatterns() : methodEntry.getKey().getPatternValues();
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

}
