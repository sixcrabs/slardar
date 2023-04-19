package cn.piesat.nj.slardar.core;

import cn.piesat.nj.slardar.core.entity.Account;

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
     * @param accountName
     * @param realm
     * @return
     */
    Account findByName(String accountName, String realm);

    /**
     * find by openid (pk)
     * @param openId
     * @return
     */
    Account findByOpenId(String openId);
}
