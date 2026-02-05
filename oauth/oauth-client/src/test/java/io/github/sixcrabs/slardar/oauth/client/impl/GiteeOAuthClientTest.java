package io.github.sixcrabs.slardar.oauth.client.impl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.winterfell.misc.keystore.KeyStoreProperties;
import org.winterfell.misc.keystore.SimpleKeyStoreFactory;
import io.github.sixcrabs.slardar.oauth.client.OAuthUser;
import io.github.sixcrabs.slardar.oauth.client.result.OAuthResult;
import io.github.sixcrabs.slardar.oauth.client.support.OAuthServerInfo;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/12/8
 */
public class GiteeOAuthClientTest {

    static GiteeOAuthClient client;

    @BeforeAll
    static void init() {
        KeyStoreProperties properties = new KeyStoreProperties();
        properties.setType("redis");
        properties.setUri("redis://127.0.0.1:6379/0");
        String baseUrl = "https://gitee.com";
        client = GiteeOAuthClient.builder()
                .clientId("352485bfad383ac9491cb360c53a846fef9cd4733906fbdcfec70f5ea5b4169c")
                .clientSecret("714ff38ebba3639a93fa61686ebf654f1f1be85bd8a3576ff836ac6f92ef3c24")
                .redirectUri("http://www.baidu.com")
                .keyStore(SimpleKeyStoreFactory.getInstance().getKeyStore(properties))
                .config(OAuthServerInfo.builder()
                        .authorizeUrl(baseUrl + "/oauth/authorize")
                        .accessTokenUrl(baseUrl + "/oauth/token")
                        .refreshTokenUrl(baseUrl + "/oauth/token")
                        .userInfoUrl(baseUrl + "/api/v5/user")
                        .build())
                .build();
    }

    @Test
    void getAccessToken() {
        String authorizeUrl = client.getAuthorizeUrl();
        System.out.println(authorizeUrl);
    }

    @Test
    void testLogin() {
        String code = "6bb7dd22f6904423f5f5be1e0b9b9b336325199587e54daec77ea59a1dea7eac";
        String state = "gy1406";
        OAuthResult<OAuthUser> authResult = client.login(code, state);
        System.out.println(authResult);
    }
}