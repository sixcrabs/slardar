package org.winterfell.slardar.starter.filter.request;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.winterfell.misc.hutool.mini.StringUtil;
import org.winterfell.misc.keystore.SimpleKeyStore;
import org.winterfell.misc.timer.cron.DateTimeUtil;
import org.winterfell.slardar.core.SlardarException;
import org.winterfell.slardar.core.annotation.SlardarIgnore;
import org.winterfell.slardar.core.domain.Client;
import org.winterfell.slardar.starter.provider.ClientProvider;
import org.winterfell.slardar.core.SlardarContext;
import org.winterfell.slardar.spi.SlardarSpiFactory;
import org.winterfell.slardar.starter.SlardarProperties;
import org.winterfell.slardar.starter.support.SignatureUtil;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.winterfell.slardar.starter.support.HttpServletUtil.*;

/**
 * <p>
 * api 签名认证方式
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/12/3
 */
public class SlardarApiSignatureFilter extends OncePerRequestFilter {

    private final SlardarContext spiContext;

    private final SlardarProperties.ApiSignatureSetting apiSignatureSetting;

    private final SimpleKeyStore keyStore;

    /**
     * 执行过滤的请求匹配器
     */
    private final List<PathPatternRequestMatcher> requestMatchers = new ArrayList<>(1);

    public SlardarApiSignatureFilter(SlardarContext spiContext, SlardarSpiFactory spiFactory,
                                     SlardarProperties slardarProperties) {
        this.spiContext = spiContext;
        this.apiSignatureSetting = slardarProperties.getSignature();
        this.keyStore = spiContext.getBean(SimpleKeyStore.class);
        String[] filterUrls = apiSignatureSetting.getFilterUrls();
        if (filterUrls != null && filterUrls.length > 0) {
            Arrays.stream(filterUrls).forEach(url -> requestMatchers.add(PathPatternRequestMatcher.withDefaults().matcher(url)));
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
        requestMatchers.add(StringUtil.isBlank(method) ? PathPatternRequestMatcher.withDefaults().matcher(antPattern) :
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.valueOf(method), antPattern));
    }

    /**
     * 哪些不需要经过 此过滤器
     *
     * @param request current HTTP request
     * @return whether the given request should <i>not</i> be filtered
     * @throws ServletException in case of errors
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
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
        // 判断时间是否大于设定的超时时间 (防止重放攻击)
        long NONCE_STR_TIMEOUT_SECONDS = apiSignatureSetting.getNonceTimeoutSeconds();
        if (Duration.between(DateTimeUtil.toDateTime(requestTime), LocalDateTime.now()).getSeconds() > NONCE_STR_TIMEOUT_SECONDS) {
            exception = new SlardarException("invalid  requestTime");
        }
        // 判断该用户的nonceStr参数是否已经在redis中（防止短时间内的重放攻击）
        if (keyStore.has(nonce)) {
            exception = new SlardarException("nonce 参数已存在, 请求非法！");
        }
        keyStore.setex(nonce, appKey, NONCE_STR_TIMEOUT_SECONDS);
        ClientProvider clientProvider = spiContext.getBeanIfAvailable(ClientProvider.class);
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
            sendError(request, response, HttpStatus.UNAUTHORIZED, exception);
        } else {
            chain.doFilter(request, response);
        }
    }

    private String getHeaderValue(final HttpServletRequest request, String headerName) {
        String headerKeyPrefix = this.apiSignatureSetting.getHeaderKeyPrefix();
        return request.getHeader(headerKeyPrefix + headerName);
    }

}