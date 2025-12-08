package org.winterfell.slardar.oauth.client.impl;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.winterfell.misc.hutool.mini.MapUtil;
import org.winterfell.slardar.oauth.client.OAuthException;
import org.winterfell.slardar.oauth.client.OAuthUser;
import org.winterfell.slardar.oauth.client.token.OAuthBaseToken;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.winterfell.slardar.oauth.client.support.OAuthUtil.*;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/12/8
 */
@SuperBuilder
@ToString(callSuper = true)
@Getter
public class GiteeOAuthClient extends AbstractOAuthClient<GiteeOAuthClient.OAuthGiteeToken> {
    /**
     * 获取 access token
     * 子类需要实现该方法
     *
     * @param code
     * @return
     * @throws OAuthException
     */
    @Override
    public OAuthGiteeToken getAccessToken(String code) throws OAuthException {
        HashMap<String, String> headers = MapUtil.of("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        JsonObject response = postAndParse(accessTokenUrl(code), null, headers);
        if (response.has("error")) {
            throw new OAuthException(response.get("error_description").getAsString());
        }
        return OAuthGiteeToken.builder()
                .accessToken(response.get("access_token").getAsString())
                .refreshToken(response.get("refresh_token").getAsString())
                .scopes(response.get("scope").isJsonNull() ? Collections.emptyList() : Lists.newArrayList(response.get("scope").getAsString().split(" ")))
                .tokenType(response.get("token_type").getAsString())
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
    public OAuthUser getUseProfile(OAuthGiteeToken token) throws OAuthException {
        JsonObject res = getAndParse(userInfoUrl(token), null);
        if (res.has("error")) {
            throw new OAuthException(res.get("error").getAsString());
        }
        return OAuthUser.builder()
                .rawInfo(res)
                .uuid(res.get("id").getAsString())
                .username(res.get("login").getAsString())
                .avatar(res.get("avatar_url").getAsString())
                .nickname(res.get("name").getAsString())
                .email(res.get("email").isJsonNull() ? "" : res.get("email").getAsString())
                .gender(res.get("gender").getAsString())
                .remark(res.get("bio").getAsString())
                .token(token)
                .blog(res.get("blog").getAsString())
                .build();
    }


    /**
     * 获取平台名称
     *
     * @return
     */
    @Override
    public String name() {
        return "gitee";
    }

    @SuperBuilder
    public static class OAuthGiteeToken extends OAuthBaseToken {
        private List<String> scopes;
        private String tokenType;
        private String refreshToken;
    }
}