package cn.piesat.v.slardar.core.provider;

import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.core.entity.Account;

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
    Account findByName(String accountName, String realm) throws SlardarException;

    /**
     * find by openid (pk)
     * @param openId
     * @return
     */
    Account findByOpenId(String openId) throws SlardarException;
}
