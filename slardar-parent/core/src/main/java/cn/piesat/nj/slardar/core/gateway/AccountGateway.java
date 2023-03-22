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
     * 此时不需要租户 因为 openid 唯一
     *
     * @param openId
     * @return
     */
    Account findByOpenId(String openId) throws Exception;

    /**
     * find by name
     * @param name
     * @param realm
     * @return
     */
    Account findByName(String name, String realm) throws Exception;
}
