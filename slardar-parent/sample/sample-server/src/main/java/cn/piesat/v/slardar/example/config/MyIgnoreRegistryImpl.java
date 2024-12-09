package cn.piesat.v.slardar.example.config;

import cn.piesat.v.slardar.starter.config.SlardarIgnoringCustomizer;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.stereotype.Component;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/4/11
 */
@Component
public class MyIgnoreRegistryImpl implements SlardarIgnoringCustomizer {
    @Override
    public void customize(WebSecurity.IgnoredRequestConfigurer configurer) {
        configurer.antMatchers("/api/greeting/**");
    }
}
