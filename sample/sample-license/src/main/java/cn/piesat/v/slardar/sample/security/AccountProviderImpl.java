package cn.piesat.v.slardar.sample.security;

import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.core.entity.Account;
import cn.piesat.v.slardar.core.provider.AccountProvider;
import org.springframework.stereotype.Component;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/9/22
 */
@Component
public class AccountProviderImpl implements AccountProvider {
 /**
  * find by name (and realm)
  *
  * @param accountName
  * @param realm
  * @return
  */
 @Override
 public Account findByName(String accountName, String realm) throws SlardarException {
  return null;
 }

 /**
  * find by openid (pk)
  *
  * @param openId
  * @return
  */
 @Override
 public Account findByOpenId(String openId) throws SlardarException {
  return null;
 }
}
