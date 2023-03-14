package cn.piesat.nj.slardar.core.gateway;

import cn.piesat.nj.slardar.core.entity.Account;
import cn.piesat.nj.slardar.core.gateway.core.CurdGateway;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/14
 */
public interface AccountGateway extends CurdGateway<String, Account> {

    /**
     * find by openid
     *
     * @param openId
     * @param realm
     * @return
     */
    Account findByOpenId(String openId, String realm);

    /**
     * find by name
     * @param name
     * @param realm
     * @return
     */
    Account findByName(String name, String realm);
}
