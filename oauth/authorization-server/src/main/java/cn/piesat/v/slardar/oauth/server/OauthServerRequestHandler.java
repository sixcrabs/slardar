package cn.piesat.v.slardar.oauth.server;

import cn.piesat.v.misc.hutool.mini.StringUtil;
import cn.piesat.v.slardar.oauth.server.config.OauthServerProperties;
import cn.piesat.v.slardar.oauth.server.support.OauthServerHandlerMapping;
import org.winterfell.slardar.spi.SlardarSpiContext;
import org.winterfell.slardar.starter.SlardarAuthenticateService;
import org.winterfell.slardar.starter.config.SlardarIgnoringCustomizer;

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

    private final SlardarSpiContext context;

    public OauthServerRequestHandler(OauthServerProperties serverProperties, SlardarSpiContext spiContext) {
        this.serverProperties = serverProperties;
        this.authenticateService = spiContext.getBean(SlardarAuthenticateService.class);
        this.context = spiContext;
    }

    /**
     * 处理/oauth/ 的各请求
     *
     * @param request
     * @param response
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        // 根据不同 path 分发处理
        String mapping = uri.replace(serverProperties.getCtxPath(), "").replaceFirst("/", "");
        OauthServerHandlerMapping handlerMapping = OauthServerHandlerMapping.valueOf(mapping);
        switch (handlerMapping) {
            case authorize:
                // TODO:
                handleAuthorize(request, response);
                break;
            case token:
                break;
            case refresh:
                break;
            case revoke:
                break;
            case userdetails:
            case userDetails:
                break;
        }

    }

    private void handleAuthorize(HttpServletRequest request, HttpServletResponse response) {
        // 尝试从请求里面读取 token 并验证是否有效
        String tokenValue = authenticateService.getTokenValueFromServlet(request);
        if (StringUtil.isBlank(tokenValue)) {
            // 跳转到SSO登录页
//            sendForward(request, response, SSO_LOGIN_VIEW_URL);
        }
    }

    @Override
    public void customize(List<String> antPatterns) {
        // 忽略 `/oauth` 相关的 url pattern
        antPatterns.add(serverProperties.getOauthAntUrlPattern());
//        antPatterns.add(SSO_LOGIN_VIEW_URL);
    }
}
