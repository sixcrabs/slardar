package cn.piesat.v.slardar.sso.client;

import cn.piesat.v.slardar.core.SlardarSecurityHelper;
import cn.piesat.v.slardar.core.entity.Account;
import cn.piesat.v.slardar.sso.client.config.SsoClientProperties;
import cn.piesat.v.slardar.sso.client.config.client.RestApiResult;
import cn.piesat.v.slardar.sso.client.config.client.SsoServerApiClient;
import cn.piesat.v.slardar.sso.client.support.SsoClientHandlerMapping;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

import static cn.piesat.v.slardar.sso.client.support.HttpServletUtil.*;

/**
 * <p>
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/30
 */
@Component
public class SsoClientTokenFilter extends OncePerRequestFilter {

    private final SsoClientProperties clientProperties;

    @Resource
    private SsoServerApiClient serverClient;

    public SsoClientTokenFilter(SsoClientProperties clientProperties) {
        this.clientProperties = clientProperties;
    }


    /**
     * 排除 {@link SsoClientRequestFilter} 处理的请求
     *
     * @param request
     * @return
     * @throws ServletException
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();
        return (uri.startsWith(clientProperties.getCtxPath()) && !uri.startsWith(clientProperties.getCtxPath().concat("/" + SsoClientHandlerMapping.isLogin.name())))
                || uri.startsWith("/doc.html")
                || uri.startsWith("/v2/api-docs")
                || uri.startsWith("/swagger-resources")
                || uri.startsWith("/webjars")
                || uri.startsWith("/favicon.ico")
                || isIgnored(uri);
    }


    private boolean isIgnored(String uri) {
        String[] ignores = clientProperties.getIgnores();
        // TBD: 验证方法有待完善
        return Arrays.stream(ignores).anyMatch(uri::startsWith);
    }

    /**
     * Same contract as for {@code doFilter}, but guaranteed to be
     * just invoked once per request within a single request thread.
     * See {@link #shouldNotFilterAsyncDispatch()} for details.
     * <p>Provides HttpServletRequest and HttpServletResponse arguments instead of the
     * default ServletRequest and ServletResponse ones.
     *
     * @param request
     * @param response
     * @param chain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            String tokenValue = getTokenValue(request);
            if (StringUtils.isEmpty(tokenValue)) {
                sendJson(response, makeErrorResult("token is required", HttpStatus.UNAUTHORIZED.value()), HttpStatus.UNAUTHORIZED);
                return;
            } else {
                // /sso/userdetails 拿到用户信息 进行填充
                try {
                    RestApiResult<Account> apiResult = serverClient.getUserDetails(tokenValue, request.getHeader("User-Agent"));
                    if (apiResult.isSuccessful()) {
                        SlardarSecurityHelper.SecurityContext context = SlardarSecurityHelper.getContext();
                        context.setAccount(apiResult.getData());
                        context.setAuthenticated(true);
                        context.setUserProfile(apiResult.getData().getUserProfile());
                    } else {
                        sendJson(response, makeErrorResult(apiResult.getMessage(), HttpStatus.UNAUTHORIZED.value()), HttpStatus.UNAUTHORIZED);
                        return;
                    }
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage());
                    sendJson(response, makeErrorResult(e.getLocalizedMessage(), HttpStatus.UNAUTHORIZED.value()), HttpStatus.UNAUTHORIZED);
                    return;
                }
            }
            chain.doFilter(request, response);
        } finally {
            SlardarSecurityHelper.clear();
        }
    }
}
