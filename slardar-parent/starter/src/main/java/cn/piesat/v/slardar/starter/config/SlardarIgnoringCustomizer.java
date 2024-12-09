package cn.piesat.v.slardar.starter.config;

import org.springframework.security.config.annotation.web.builders.WebSecurity;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
@FunctionalInterface
public interface SlardarIgnoringCustomizer {

    /**
     * 自定义过滤需要忽略的url等
     * @param configurer
     */
    void customize(WebSecurity.IgnoredRequestConfigurer configurer);
}
