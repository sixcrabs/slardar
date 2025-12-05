package org.winterfell.slardar.oauth.client.impl;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.winterfell.slardar.oauth.client.OAuthException;
import org.winterfell.slardar.oauth.client.OAuthUser;
import org.winterfell.slardar.oauth.client.token.OAuthBaseToken;

/**
 * <p>
 * TODO
 * 适配 slardar oauth 平台
 * </p>
 *
 * @author Alex
 * @since 2025/12/5
 */
@SuperBuilder
@ToString(callSuper = true)
@Getter
public class SlardarOAuthClient extends AbstractOAuthClient<OAuthBaseToken> {

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
        return null;
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
        return null;
    }

    /**
     * 获取平台名称
     *
     * @return
     */
    @Override
    public String name() {
        return "slardar";
    }
}