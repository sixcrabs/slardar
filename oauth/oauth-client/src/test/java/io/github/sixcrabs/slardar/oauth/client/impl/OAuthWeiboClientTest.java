package io.github.sixcrabs.slardar.oauth.client.impl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.winterfell.misc.keystore.KeyStoreProperties;
import org.winterfell.misc.keystore.SimpleKeyStoreFactory;
import io.github.sixcrabs.slardar.oauth.client.OAuthUser;
import io.github.sixcrabs.slardar.oauth.client.result.OAuthResult;
import io.github.sixcrabs.slardar.oauth.client.support.OAuthServerInfo;

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
    static WeiboOAuthClient weiboClient;

    @BeforeAll
    static void beforeAll() {
        String baseUrl = "https://api.weibo.com";
        KeyStoreProperties properties = new KeyStoreProperties();
        properties.setType("redis");
        properties.setUri("redis://127.0.0.1:6379/0");
        weiboClient = WeiboOAuthClient.builder()
                .clientId("3649223367")
                .clientSecret("6ecad127dc9b374011b88cb37bd761ad")
                .redirectUri("http://www.baidu.com")
                .scopes(Arrays.asList("all", "email"))
                .config(OAuthServerInfo.builder()
                        .authorizeUrl(baseUrl + "/oauth2/authorize")
                        .accessTokenUrl(baseUrl + "/oauth2/access_token")
                        .userInfoUrl(baseUrl + "/2/users/show.json")
                        .revokeUrl(baseUrl + "/oauth2/revokeoauth2")
                        .build())
                .keyStore(SimpleKeyStoreFactory.getInstance().getKeyStore(properties))
                .build();
    }

    @Test
    void testGetAuthorizeUrl() {
        String authorizeUrl = weiboClient.getAuthorizeUrl();
        System.out.println(authorizeUrl);
    }

    @Test
    void testLogin() {
        String code = "354d2d42027cd7d691a6cef1d7914158";
        String state = "2kwut1";
        OAuthResult<OAuthUser> loginResult = weiboClient.login(code, state);
        System.out.println(loginResult);
    }
}