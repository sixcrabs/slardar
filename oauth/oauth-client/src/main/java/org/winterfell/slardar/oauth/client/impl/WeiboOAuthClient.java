package org.winterfell.slardar.oauth.client.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.winterfell.misc.hutool.mini.MapUtil;
import org.winterfell.slardar.oauth.client.OAuthException;
import org.winterfell.slardar.oauth.client.OAuthUser;
import org.winterfell.slardar.oauth.client.result.OAuthResult;
import org.winterfell.slardar.oauth.client.result.OAuthResultStatus;
import org.winterfell.slardar.oauth.client.support.HttpUrlBuilder;
import org.winterfell.slardar.oauth.client.token.OAuthBaseToken;

import java.util.Map;

import static org.winterfell.slardar.oauth.client.support.OAuthUtil.*;

/**
 * <p>
 * 适配 微博 oauth平台
 * </p>
 *
 * @author Alex
 * @since 2025/12/5
 */
@SuperBuilder
@ToString(callSuper = true)
@Getter
public class WeiboOAuthClient extends AbstractOAuthClient<OAuthBaseToken> {

    /**
     * 获取平台名称
     *
     * @return
     */
    @Override
    public String name() {
        return "weibo";
    }

    /**
     * 获取授权跳转url, 需要加上 scopes 参数
     *
     * @return
     * @throws OAuthException
     */
    @Override
    public String getAuthorizeUrl() throws OAuthException {
        return HttpUrlBuilder.fromBaseUrl(super.getAuthorizeUrl()
                .concat("&scope=".concat( String.join(",", super.scopes)))).build();
    }

    /**
     * 获取 access token
     * 子类需要实现该方法
     *
     * @param code
     * @return
     * @throws OAuthException
     */
    @Override
    public OAuthBaseToken getAccessToken(String code) throws OAuthException {
        JsonObject response = postAndParse(accessTokenUrl(code), null);
        if (response.has("error")) {
            throw new OAuthException(response.get("error_description").getAsString());
        }
        return OAuthBaseToken.builder()
                .accessToken(response.get("access_token").getAsString())
                .uid(response.get("uid").getAsString())
                .openId(response.get("uid").getAsString())
                .expireIn(response.get("expires_in").getAsInt())
                .build();
    }

    /**
     * 获取用户信息
     *
     * @param token
     * @return
     * @throws OAuthException
     */
    @Override
    public OAuthUser getUseProfile(OAuthBaseToken token) throws OAuthException {
        Map<String, String> headers = MapUtil.of("API-RemoteIP", getLocalIp());
        JsonObject res = getAndParse(userInfoUrl(token), headers);
        if (res.has("error")) {
            throw new OAuthException(res.get("error").getAsString());
        }

        return OAuthUser.builder()
                .rawInfo(res)
                .uuid(res.get("id").getAsString())
                .username(res.get("name").getAsString())
                .avatar(res.get("profile_image_url").getAsString())
                .nickname(res.get("screen_name").getAsString())
                .location(res.get("location").getAsString())
                .remark(res.get("description").getAsString())
                .gender(res.get("gender").getAsString())
                .token(token)
                .blog(res.get("url").getAsString())
                .build();
    }

    /**
     * 撤销授权 weibo 支持
     *
     * @param token
     * @return 撤销 成功/失败
     * @throws OAuthException
     */
    @Override
    public OAuthResult<Void> revoke(OAuthBaseToken token) throws OAuthException {
        String revokeUrl = revokeUrl(token);
        JsonObject response = getAndParse(revokeUrl, null);
        if (response.has("error")) {
            return OAuthResult.error(response.get("error").getAsString());
        }
        // 返回 result = true 表示取消授权成功，否则失败
        return response.get("result").getAsBoolean() ?  OAuthResult.success():
                OAuthResult.error(OAuthResultStatus.FAILURE.getCode(), "取消授权失败");
    }

    /**
     * 返回获取userInfo的url
     *
     * @param authToken token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(OAuthBaseToken authToken) {
        return HttpUrlBuilder.fromBaseUrl(config.getUserInfoUrl())
                .queryParam("access_token", authToken.getAccessToken())
                .queryParam("uid", authToken.getUid())
                .build();
    }
}