package io.github.sixcrabs.slardar.oauth.client.token;

import java.io.Serializable;

/**
 * <p>
 * token 接口定义，各平台自行实现该接口
 * </p>
 *
 * @author Alex
 * @since 2025/12/5
 */
public interface OAuthToken extends Serializable {

    String getAccessToken();

    int getExpireIn();

    String getRefreshToken();

    int getRefreshTokenExpireIn();

    String getUid();

    String getOpenId();

    String getAccessCode();

    String getUnionId();
}