package cn.piesat.nj.slardar.sso.server;

import cn.piesat.nj.slardar.sso.server.config.SsoServerProperties;
import cn.piesat.nj.slardar.starter.config.SlardarIgnoringCustomizer;
import org.springframework.security.config.annotation.web.builders.WebSecurity;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
public class SsoServerHandler implements SlardarIgnoringCustomizer {

    private final SsoServerProperties serverProperties;

    public SsoServerHandler(SsoServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    @Override
    public void customize(WebSecurity.IgnoredRequestConfigurer configure) {
        // 忽略 `/sso` 相关的 url pattern
        configure.antMatchers(serverProperties.getCtxPath().concat("/**"));
    }
}
