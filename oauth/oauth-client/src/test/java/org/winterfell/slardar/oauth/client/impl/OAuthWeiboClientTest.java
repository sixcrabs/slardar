package org.winterfell.slardar.oauth.client.impl;

import org.junit.jupiter.api.Test;
import org.winterfell.slardar.oauth.client.OAuthUser;
import org.winterfell.slardar.oauth.client.result.OAuthResult;
import org.winterfell.slardar.oauth.client.support.OAuthServerInfo;

import java.util.Arrays;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/12/5
 */
public class OAuthWeiboClientTest {

    @Test
    void name() {
        String baseUrl = "https://api.weibo.com";
        WeiboOAuthClient weiboClient = WeiboOAuthClient.builder()
                .clientId("clientId")
                .clientSecret("clientSecret")
                .redirectUri("redirectUri")
                .scopes(Arrays.asList("scope1", "scope2"))
                .config(OAuthServerInfo.builder()
                        .authorizeUrl(baseUrl + "/oauth2/authorize")
                        .accessTokenUrl(baseUrl + "/oauth2/access_token")
                        .userInfoUrl(baseUrl + "/2/users/show.json")
                        .revokeUrl(baseUrl + "/oauth2/revokeoauth2")
                        .build())
                .build();

        String authorizeUrl = weiboClient.getAuthorizeUrl();
        System.out.println(authorizeUrl);
        OAuthResult<OAuthUser> loginResult = weiboClient.login("code", "xxxx");
        System.out.println(loginResult);
    }
}