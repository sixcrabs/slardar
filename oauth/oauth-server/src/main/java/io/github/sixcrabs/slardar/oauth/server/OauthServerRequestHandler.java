package io.github.sixcrabs.slardar.oauth.server;

import io.github.sixcrabs.slardar.core.SlardarContext;
import io.github.sixcrabs.slardar.starter.authenticate.SlardarAuthenticateService;
import io.github.sixcrabs.slardar.starter.config.customizer.SlardarIgnoringCustomizer;
import org.winterfell.misc.hutool.mini.StringUtil;
import io.github.sixcrabs.slardar.oauth.server.config.OauthServerProperties;
import io.github.sixcrabs.slardar.oauth.server.support.OauthConstants;
import io.github.sixcrabs.slardar.oauth.server.support.OauthServerException;
import io.github.sixcrabs.slardar.oauth.server.support.OauthServerHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 处理 /oauth2/ 请求
 * </p>
 *
 * @author Alex
 * @since 2025/4/11
 */
public class OauthServerRequestHandler implements SlardarIgnoringCustomizer {

    private final OauthServerProperties serverProperties;

    private final SlardarAuthenticateService authenticateService;

    private final SlardarContext context;

    public OauthServerRequestHandler(OauthServerProperties serverProperties, SlardarContext spiContext) {
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
        try {
            switch (handlerMapping) {
                case authorize:
                    // 授权码模式
                    authorizeByCode(request, response);
                    break;
                case token:
                    break;
                case refresh:
                    break;
                case revoke:
                    break;
                case client_token:
                    break;
                case profile:
                case userDetails:
                    break;
            }
        } catch (OauthServerException e) {
            // TODO: 响应失败
            throw new RuntimeException(e);
        }

    }

    /**
     * 授权码模式验证
     * @param request
     * @param response
     */
    private void authorizeByCode(HttpServletRequest request, HttpServletResponse response) throws OauthServerException {
        // 读取相关参数
        String responseType = request.getParameter(OauthConstants.RequestParam.response_type);
        String clientId = request.getParameter(OauthConstants.RequestParam.client_id);
        String redirectUri = request.getParameter(OauthConstants.RequestParam.redirect_uri);
        String scope = request.getParameter(OauthConstants.RequestParam.scope);
        if (StringUtil.isBlank(responseType)) {
            throw new OauthServerException("response_type 参数不能为空");
        }
        if (!OauthConstants.ResponseType.code.equalsIgnoreCase(responseType)) {
            throw new OauthServerException("response_type 参数错误, 目前仅支持授权码模式");
        }



    }

    @Override
    public void customize(List<String> antPatterns) {
        // 忽略 `/oauth` 相关的 url pattern
        antPatterns.add(serverProperties.getOauthAntUrlPattern());
//        antPatterns.add(SSO_LOGIN_VIEW_URL);
    }
}