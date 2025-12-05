package org.winterfell.slardar.oauth.client.impl;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.winterfell.slardar.oauth.client.OAuthException;
import org.winterfell.slardar.oauth.client.OAuthUser;
import org.winterfell.slardar.oauth.client.result.OAuthNoneResult;
import org.winterfell.slardar.oauth.client.support.OAuthServerInfo;
import org.winterfell.slardar.oauth.client.token.OAuthBaseToken;

/**
 * <p>
 * .TODO
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
        return "weibo";
    }

    /**
     * 撤销授权 部分平台支持
     *
     * @param token
     * @return 撤销 成功/失败
     * @throws OAuthException
     */
    @Override
    public OAuthNoneResult revoke(OAuthBaseToken token) throws OAuthException {
        return super.revoke(token);
    }

}