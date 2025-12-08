package org.winterfell.slardar.oauth.client.token;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * token 通用的属性定义
 * </p>
 *
 * @author Alex
 * @since 2025/12/5
 */
@Data
@SuperBuilder
public class OAuthBaseToken implements OAuthToken {

    private String accessToken;
    private int expireIn;
    private String refreshToken;
    private int refreshTokenExpireIn;
    private String uid;
    private String openId;
    private String accessCode;
    private String unionId;
}