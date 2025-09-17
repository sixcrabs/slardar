package cn.piesat.v.slardar.sso.client;

import cn.piesat.v.slardar.core.SlardarSecurityHelper;
import cn.piesat.v.slardar.sso.client.config.SsoClientProperties;
import cn.piesat.v.slardar.sso.client.config.client.RestApiResult;
import cn.piesat.v.slardar.sso.client.config.client.SsoServerApiClient;
import cn.piesat.v.slardar.sso.client.support.HttpServletUtil;
import cn.piesat.v.slardar.sso.client.support.SsoClientHandlerMapping;
import cn.piesat.v.slardar.sso.client.support.SsoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static cn.piesat.v.slardar.sso.client.support.HttpServletUtil.*;

/**
 * <p>
 * sso-client request handler
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/30
 */
public class SsoClientRequestHandler {

    private static final Logger log = LoggerFactory.getLogger(SsoClientRequestHandler.class);


    private final SsoClientProperties clientProperties;

    @Resource
    private SsoServerApiClient serverClient;

    public SsoClientRequestHandler(SsoClientProperties clientProperties) {
        this.clientProperties = clientProperties;
    }

    /**
     * 处理 sso-client 请求
     *
     * @param request
     * @param response
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        // 根据不同 path 分发处理
        String mapping = uri.replace(clientProperties.getCtxPath(), "").replaceFirst("/", "");
        try {
            switch (SsoClientHandlerMapping.valueOf(mapping)) {
                case isLogin:
                    isLogin(request, response);
                    break;
                case doLogout:
                    // TODO: /sso/logout
                    break;
                case getSsoAuthUrl:
                    getSsoAuthUrl(request, response);
                    break;
                case doLogin:
                    // 使用ticket 登录 登录成功后 进行跳转
                    doLoginByTicket(request, response);
                    break;
                default:
                    throw new SsoException("error");
            }
        } catch (SsoException e) {
            sendJson(response, makeErrorResult(e.toString(), e.getCode() > 0 ? e.getCode() : 4001), HttpStatus.OK);
        }
    }

    /**
     * 解析传参 `url`
     *
     * @param request
     * @param response
     */
    private void getSsoAuthUrl(HttpServletRequest request, HttpServletResponse response) {
        String ssoServer = clientProperties.getServerUrl();
        String redirectUrl = getParam(request, "url");

        sendJsonOk(response, makeSuccessResult(ssoServer.concat("/auth?url=" + redirectUrl)));
    }

    /**
     * genju token 验证是否已登录
     *
     * @param request
     * @param response
     */
    private void isLogin(HttpServletRequest request, HttpServletResponse response) throws SsoException {
        String tokenValue = getTokenValue(request);
        if (StringUtils.isEmpty(tokenValue)) {
            throw new SsoException("Token is empty").setCode(HttpStatus.UNAUTHORIZED.value());
        } else {
            sendJsonOk(response, makeSuccessResult(SlardarSecurityHelper.getContext().isAuthenticated()));
        }
    }

    /**
     * login by ticket
     *
     * @param request
     * @param response
     */
    private void doLoginByTicket(HttpServletRequest request, HttpServletResponse response) throws SsoException {
        // 通过 rest api向 sso server 验证 ticket
        String ticket = getParam(request, "ticket");
        try {
            RestApiResult<String> apiResult = serverClient.checkTicket(ticket);
            if (apiResult.isSuccessful()) {
                // 返回 token
                String accessToken = apiResult.getData();
                // set cookie
                HttpServletUtil.setCookie(response, "Authorization", accessToken, 0, "", "", "");
                sendJsonOk(response, accessToken);
            } else {
                throw new SsoException(apiResult.getMessage());
            }
        } catch (Exception e) {
            throw new SsoException(e.getLocalizedMessage());
        }
    }


}
