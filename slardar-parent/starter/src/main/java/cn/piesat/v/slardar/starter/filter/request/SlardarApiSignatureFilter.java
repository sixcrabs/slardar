package cn.piesat.v.slardar.starter.filter.request;

import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.core.annotation.SlardarIgnore;
import cn.piesat.v.slardar.core.entity.Client;
import cn.piesat.v.slardar.core.provider.ClientProvider;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.starter.config.SlardarProperties;
import cn.piesat.v.slardar.starter.support.SignatureUtil;
import cn.piesat.v.misc.hutool.mini.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static cn.piesat.v.slardar.starter.support.HttpServletUtil.makeErrorResult;
import static cn.piesat.v.slardar.starter.support.HttpServletUtil.sendJson;

/**
 * <p>
 * api 签名认证方式
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/12/3
 */
public class SlardarApiSignatureFilter extends OncePerRequestFilter {

    private final SlardarSpiContext spiContext;

    private final SlardarProperties.ApiSignatureSetting apiSignatureSetting;

    /**
     * 执行过滤的请求匹配器
     */
    private final List<AntPathRequestMatcher> requestMatchers = new ArrayList<>(1);

    public SlardarApiSignatureFilter(SlardarSpiContext spiContext, SlardarProperties.ApiSignatureSetting apiSignatureSetting) {
        this.spiContext = spiContext;
        this.apiSignatureSetting = apiSignatureSetting;
        String[] filterUrls = apiSignatureSetting.getFilterUrls();
        if (filterUrls != null && filterUrls.length > 0) {
            Arrays.stream(filterUrls).forEach(url -> requestMatchers.add(new AntPathRequestMatcher(url)));
        }
    }

    /**
     * 外部添加过滤的 url pattern
     *
     * @param antPattern
     * @param method
     * @see SlardarIgnore
     */
    public void addUrlPattern(String antPattern, String method) {
        requestMatchers.add(new AntPathRequestMatcher(antPattern, StringUtil.isBlank(method) ? null : method));
    }

    /**
     * 哪些不需要经过 此过滤器
     *
     * @param request current HTTP request
     * @return whether the given request should <i>not</i> be filtered
     * @throws ServletException in case of errors
     */
    @Override
    protected boolean shouldNotFilter(@NotNull HttpServletRequest request) throws ServletException {
        return requestMatchers.stream().noneMatch(matcher -> matcher.matcher(request).isMatch());
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
        SlardarException exception = null;
        String path = request.getRequestURI();
        // 解析 header
        String appKey = getHeaderValue(request, "AppKey");
        if (StringUtil.isBlank(appKey)) {
            exception = new SlardarException("appKey 不能为空");
        }
        String nonce = getHeaderValue(request, "Nonce");
        if (StringUtil.isBlank(nonce)) {
            exception = new SlardarException("nonce 不能为空");
        }
        String requestTime = getHeaderValue(request, "RequestTime");
        if (StringUtil.isBlank(requestTime)) {
            exception = new SlardarException("requestTime 不能为空");
        }
        String sign = getHeaderValue(request, "Signature");
        if (StringUtil.isBlank(sign)) {
            exception = new SlardarException("签名不能为空");
        }
        ClientProvider clientProvider = spiContext.getClientProvider();
        if (Objects.isNull(clientProvider)) {
            exception = new SlardarException("ClientProvider 需要被实现");
        } else {
            // 验证 appKey
            Client client = clientProvider.findByClientId(appKey);
            if (client == null) {
                exception = new SlardarException("appKey 对应的client不存在!");
            } else {
                // 验证将要访问的接口path 是否被允许
                List<String> clientScopes = client.getClientScopes();
                if (clientScopes != null && !clientScopes.isEmpty()) {
                    boolean matched = clientScopes.stream().anyMatch(pattern -> new AntPathMatcher().match(pattern, path));
                    if (!matched) {
                        exception = new SlardarException("当前 appKey 无法访问该资源");
                    }
                }
                // 计算签名
                String signature = null;
                try {
                    signature = SignatureUtil.generateSignature(path, appKey, client.getClientSecret(), nonce, requestTime);
                } catch (SlardarException e) {
                    exception = e;
                }
                if (signature == null) {
                    exception = new SlardarException("签名计算失败，检查携带的请求头参数");
                } else if (!signature.equals(sign)) {
                    exception = new SlardarException("签名不匹配");
                }
            }
        }
        if (exception != null) {
            forwardRequest(request, response, exception, "remoteLoginException", "/remoteLoginException");
        } else {
            chain.doFilter(request, response);
        }
    }

    private String getHeaderValue(final HttpServletRequest request, String headerName) {
        String headerKeyPrefix = this.apiSignatureSetting.getHeaderKeyPrefix();
        return request.getHeader(headerKeyPrefix + headerName);
    }

    /**
     * 转发
     *
     * @param request
     * @param response
     * @param e
     * @param param
     * @param url
     * @throws ServletException
     * @throws IOException
     */
    private void forwardRequest(HttpServletRequest request, HttpServletResponse response, SlardarException e, String param, String url) throws ServletException, IOException {
        request.setAttribute(param, e);
        sendJson(response, makeErrorResult(e.getLocalizedMessage(), HttpStatus.UNAUTHORIZED.value()), HttpStatus.UNAUTHORIZED, request.getHeader("Origin"));
    }
}
