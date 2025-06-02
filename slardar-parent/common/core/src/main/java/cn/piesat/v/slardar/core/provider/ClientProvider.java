package cn.piesat.v.slardar.core.provider;

import cn.piesat.v.slardar.core.entity.Client;

/**
 * <p>
 * 由应用实现 提供给认证方法查询到相关认证客户端
 * <br/>
 * 适用于
 *  <ul>
 *      <li> - sso client</li>
 *      <li> - oauth2 client</li>
 *      <li> - api signature</li>
 *  </ul>
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/12/9
 */
public interface ClientProvider {

    /**
     * find client by clientId
     * @param clientId
     * @return
     */
    Client findByClientId(String clientId);
}
