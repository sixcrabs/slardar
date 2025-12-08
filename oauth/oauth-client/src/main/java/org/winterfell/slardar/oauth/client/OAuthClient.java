package org.winterfell.slardar.oauth.client;

import org.winterfell.slardar.oauth.client.result.OAuthEmptyResult;
import org.winterfell.slardar.oauth.client.result.OAuthResult;
import org.winterfell.slardar.oauth.client.result.OAuthResultStatus;
import org.winterfell.slardar.oauth.client.token.OAuthToken;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/12/5
 */
public interface OAuthClient<T extends OAuthToken> {


    /**
     * 获取平台名称
     *
     * @return
     */
    String name();

    /**
     * 获取授权跳转url
     *
     * @return
     * @throws OAuthException
     */
    String getAuthorizeUrl() throws OAuthException;

    /**
     * 登录入口，跳转到授权页面并登录认证平台后，回调到应用服务中，调用此方法实现oauth登录
     * - 检查code
     * - 换取token
     * - 获取用户信息
     * - 返回
     *
     * @param code
     * @param state
     * @return
     * @throws OAuthException
     */
    OAuthResult<OAuthUser> login(String code, String state) throws OAuthException;

    /**
     * 撤销授权 部分平台支持
     *
     * @param token
     * @return 撤销 成功/失败
     * @throws OAuthException
     */
    default OAuthResult<Void> revoke(T token) throws OAuthException {
        throw new OAuthException(OAuthResultStatus.UNSUPPORTED);
    }

    /**
     * 刷新token 部分平台支持
     *
     * @param token
     * @return 刷新后的token结果
     * @throws OAuthException
     */
    default OAuthResult<T> refresh(T token) throws OAuthException {
        throw new OAuthException(OAuthResultStatus.UNSUPPORTED);
    }
}