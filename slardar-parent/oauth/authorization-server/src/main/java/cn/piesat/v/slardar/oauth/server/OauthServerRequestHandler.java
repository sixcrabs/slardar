package cn.piesat.v.slardar.oauth.server;

import cn.piesat.v.slardar.oauth.server.config.OauthServerProperties;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.starter.SlardarAuthenticateService;
import cn.piesat.v.slardar.starter.config.SlardarIgnoringCustomizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/4/11
 */
public class OauthServerRequestHandler implements SlardarIgnoringCustomizer {

    private final OauthServerProperties serverProperties;

    private final SlardarAuthenticateService authenticateService;

    private final SlardarSpiContext spiContext;

    public OauthServerRequestHandler(OauthServerProperties serverProperties, SlardarSpiContext spiContext) {
        this.serverProperties = serverProperties;
        this.authenticateService = spiContext.getBean(SlardarAuthenticateService.class);;
        this.spiContext = spiContext;
    }

    public void handle(HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    public void customize(List<String> antPatterns) {
        // 忽略 `/oauth` 相关的 url pattern
        antPatterns.add(serverProperties.getOauthAntUrlPattern());
//        antPatterns.add(SSO_LOGIN_VIEW_URL);
    }
}
