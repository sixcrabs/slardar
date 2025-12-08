package org.winterfell.slardar.oauth.client.impl;

import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.winterfell.misc.hutool.mini.RandomUtil;
import org.winterfell.misc.hutool.mini.StringUtil;
import org.winterfell.misc.keystore.SimpleKeyStore;
import org.winterfell.slardar.oauth.client.OAuthClient;
import org.winterfell.slardar.oauth.client.OAuthException;
import org.winterfell.slardar.oauth.client.OAuthUser;
import org.winterfell.slardar.oauth.client.result.OAuthEmptyResult;
import org.winterfell.slardar.oauth.client.result.OAuthResult;
import org.winterfell.slardar.oauth.client.result.OAuthResultStatus;
import org.winterfell.slardar.oauth.client.support.HttpUrlBuilder;
import org.winterfell.slardar.oauth.client.support.OAuthServerInfo;
import org.winterfell.slardar.oauth.client.token.OAuthBaseToken;

import java.util.List;

/**
 * <p>
 * client 实现的基类 各平台都是基于此类实现
 * </p>
 *
 * @author Alex
 * @since 2025/12/5
 */
@Slf4j
@SuperBuilder
public abstract class AbstractOAuthClient<T extends OAuthBaseToken> implements OAuthClient<T> {

    protected String clientId;
    protected String clientSecret;
    protected String redirectUri;
    protected List<String> scopes;
    /**
     * 认证接口等server参数配置
     */
    protected OAuthServerInfo config;
    /**
     * 用于存储 state 等
     */
    protected SimpleKeyStore keyStore;

    /**
     * 获取授权跳转url
     *
     * @return
     * @throws OAuthException
     */
    @Override
    public String getAuthorizeUrl() throws OAuthException {
        // state 用于避免 csrf 攻击
        String state = createState();
        HttpUrlBuilder builder = HttpUrlBuilder.fromBaseUrl(config.getAuthorizeUrl())
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state);
        return builder.build();
    }

    /**
     * 登录入口，跳转到授权页面并登录认证平台后，回调到应用服务中，调用此方法实现oauth登录
     * - 检查code
     * - 换取token
     * - 获取用户信息
     * - 返回
     *
     * @param code
     * @param state
     * @return
     * @throws OAuthException
     */
    @Override
    public OAuthResult<OAuthUser> login(String code, String state) throws OAuthException {
        try {
            preCheckCode(code);
            preCheckState(state);
            T accessToken = this.getAccessToken(code);
            OAuthUser useProfile = this.getUseProfile(accessToken);
            return OAuthResult.success(useProfile);
        } catch (Exception e) {
            log.error("Failed to login with oauth authorization.", e);
            return OAuthResult.error(e);
        }
    }

    /**
     * 获取 access token
     * 子类需要实现该方法
     *
     * @param code
     * @return
     * @throws OAuthException
     */
    public abstract T getAccessToken(String code) throws OAuthException;

    /**
     * 获取用户信息
     *
     * @param token
     * @return
     * @throws OAuthException
     */
    public abstract OAuthUser getUseProfile(T token) throws OAuthException;

    /**
     * 撤销授权 部分平台支持
     *
     * @param token
     * @return 撤销 成功/失败
     * @throws OAuthException
     */
    @Override
    public OAuthResult<Void> revoke(T token) throws OAuthException {
        return OAuthClient.super.revoke(token);
    }

    /**
     * 刷新token 部分平台支持
     *
     * @param token
     * @return 刷新后的token结果
     * @throws OAuthException
     */
    @Override
    public OAuthResult<T> refresh(T token) throws OAuthException {
        return OAuthClient.super.refresh(token);
    }

    /**
     * 自定义平台可覆盖此方法
     *
     * @param code 授权码code值
     */
    protected void preCheckCode(String code) {
        if (StringUtil.isEmpty(code)) {
            throw new OAuthException(OAuthResultStatus.ILLEGAL_CODE);
        }
    }

    /**
     * 自定义平台可覆盖此方法
     *
     * @param state state 值
     */
    protected void preCheckState(String state) {
        if (StringUtil.isEmpty(state)) {
            throw new OAuthException(OAuthResultStatus.ILLEGAL_STATE);
        }
        if (!keyStore.has(state)) {
            throw new OAuthException(OAuthResultStatus.ILLEGAL_STATE);
        }
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code 授权码
     * @return 返回获取accessToken的url
     */
    protected String accessTokenUrl(String code) {
        return HttpUrlBuilder.fromBaseUrl(config.getAccessTokenUrl())
                .queryParam("code", code)
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("grant_type", "authorization_code")
                .queryParam("redirect_uri", redirectUri)
                .build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param refreshToken refreshToken
     * @return 返回获取accessToken的url
     */
    protected String refreshTokenUrl(String refreshToken) {
        return HttpUrlBuilder.fromBaseUrl(config.getRefreshUrl())
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("refresh_token", refreshToken)
                .queryParam("grant_type", "refresh_token")
                .queryParam("redirect_uri", redirectUri)
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param authToken token
     * @return 返回获取userInfo的url
     */
    protected String userInfoUrl(OAuthBaseToken authToken) {
        return HttpUrlBuilder.fromBaseUrl(config.getUserInfoUrl())
                .queryParam("access_token", authToken.getAccessToken())
                .build();
    }

    /**
     * 返回获取revoke authorization的url
     *
     * @param authToken token
     * @return 返回获取revoke authorization的url
     */
    protected String revokeUrl(OAuthBaseToken authToken) {
        return HttpUrlBuilder.fromBaseUrl(config.getRevokeUrl())
                .queryParam("access_token", authToken.getAccessToken())
                .build();
    }


    private String createState() {
        String s = RandomUtil.randomString(6);
        // 5分钟后失效 防止被截获使用
        keyStore.setex(s, s, 60 * 5);
        return s;
    }

}