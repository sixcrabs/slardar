package io.github.sixcrabs.slardar.oauth.client.support;

import lombok.Builder;
import lombok.Getter;

/**
 * <p>
 * oauth server 地址信息等
 * </p>
 *
 * @author Alex
 * @since 2025/12/5
 */
@Getter
@Builder
public class OAuthServerInfo {

    private String authorizeUrl;
    private String accessTokenUrl;
    private String userInfoUrl;
    private String revokeUrl;
    private String refreshTokenUrl;

}