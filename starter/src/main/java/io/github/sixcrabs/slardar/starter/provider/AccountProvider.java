package io.github.sixcrabs.slardar.starter.provider;

import io.github.sixcrabs.slardar.core.SlardarException;
import io.github.sixcrabs.slardar.core.domain.Account;

/**
 * <p>
 * provide account to auth
 * </p>
 *
 * @author alex
 * @version v1.0 2023/4/19
 */
public interface AccountProvider {

    /**
     * find by name (and realm)
     *
     * @param accountName
     * @param realm
     * @return
     */
    Account findByName(String accountName, String realm) throws SlardarException;

    /**
     * find by openid (pk)
     *
     * @param openId
     * @return
     */
    Account findByOpenId(String openId) throws SlardarException;

    /**
     * set password
     *
     * @param accountName 账户名
     * @param newPwd      明文新密码
     * @param realm       域
     * @return
     * @throws SlardarException
     */
    default boolean setPwd(String accountName, String newPwd, String realm) throws SlardarException {
        return false;
    }
}