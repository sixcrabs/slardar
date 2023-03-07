package cn.piesat.nj.slardar.starter;

import cn.piesat.v.authx.security.infrastructure.spring.filter.AuthxProcessingFilter;
import cn.piesat.v.authx.security.infrastructure.spring.filter.AuthxTokenFilter;
import cn.piesat.v.authx.security.infrastructure.spring.handler.AuthxAccessDeniedHandler;
import cn.piesat.v.authx.security.infrastructure.spring.handler.AuthxAuthenticateFailedHandler;
import cn.piesat.v.authx.security.infrastructure.spring.handler.AuthxAuthenticateSucceedHandler;
import cn.piesat.v.authx.security.infrastructure.spring.handler.authentication.AuthxAuthenticationRequestHandlerFactory;
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

import static cn.piesat.v.authx.security.infrastructure.spring.SecurityPreConfiguration.STATIC_RES_MATCHERS;

/**
 * @author JiajieZhang
 * @date 2022/9/23
 * @description spring security配置类
 */

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
@AutoConfigureAfter({SecurityPreConfiguration.class})
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthxAccessDeniedHandler accessDeniedHandler;

    @Autowired
    private AuthxAuthenticateFailedHandler authtiacteFailedHandler;

    @Autowired
    private AuthxAuthenticateSucceedHandler authenticateSucceedHandler;

    @Autowired
    private AuthxAuthenticationRequestHandlerFactory authenticationRequestHandlerFactory;

    @Autowired
    private HttpFirewall httpFirewall;

    @Autowired
    private AuthxTokenFilter authzTokenFilter;

    private final SecurityProperties properties;

    private final AuthxAuthenticationProvider authenticationProvider;


    public SecurityConfig(SecurityProperties properties, AuthxAuthenticationProvider authxAuthenticationProvider) {
        this.properties = properties;
        this.authenticationProvider = authxAuthenticationProvider;
    }

    /**
     * 配置校验的方法类
     *
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
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
        //添加自定义为授权、未登录的结果返回
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry = httpSecurity.exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(authtiacteFailedHandler)
                .and()
                .csrf()
                .disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().authorizeRequests();

        registry.antMatchers(authzTokenFilter.getIgnoredUrls()).permitAll();

        registry.antMatchers(HttpMethod.OPTIONS)
                .permitAll()
                .anyRequest()
                .authenticated();

        //禁用缓存
        httpSecurity.headers().cacheControl();
        // TBD: 禁用 iframe 策略
        httpSecurity.headers().frameOptions().disable();
        // TBD: 设置filter
        httpSecurity.addFilterBefore(authzTokenFilter, UsernamePasswordAuthenticationFilter.class);
        httpSecurity.addFilterBefore(authxProcessingFilter(properties, getManagerBean(),
                authtiacteFailedHandler, authenticateSucceedHandler, authenticationRequestHandlerFactory), AuthxTokenFilter.class);


    }

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
        //url中允许使用双斜杠
        web.httpFirewall(httpFirewall);
    }

    @Bean
    public AuthxProcessingFilter authxProcessingFilter(SecurityProperties properties,
                                                       AuthenticationManager authenticationManager,
                                                       AuthenticationFailureHandler failureHandler,
                                                       AuthenticationSuccessHandler successHandler,
                                                       AuthxAuthenticationRequestHandlerFactory authenticationRequestHandlerFactory) {
        return new AuthxProcessingFilter(properties, authenticationManager, failureHandler, successHandler, authenticationRequestHandlerFactory);
    }

    @Bean
    public AuthenticationManager getManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}
