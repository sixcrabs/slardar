package org.winterfell.slardar.starter;

import org.winterfell.slardar.core.provider.AccountProvider;
import org.winterfell.slardar.core.Constants;
import org.winterfell.slardar.core.entity.Account;
import org.winterfell.slardar.spi.SlardarSpiContext;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Objects;

/**
 * <p>
 * 获取用户详细信息等
 * </p>
 *
 * @author Alex
 * @version v1.0 2022/12/14
 */
public class SlardarUserDetailsServiceImpl implements UserDetailsService {

    private final AccountProvider accountProvider;

    public SlardarUserDetailsServiceImpl(SlardarSpiContext slardarContext) {
        this.accountProvider = slardarContext.getAccountProvider();
    }


    /**
     * 根据 openid 获取用户信息
     *
     * @param openId 唯一id
     * @return
     * @throws AuthenticationException
     */
    public UserDetails loadUserByOpenId(String openId) throws AuthenticationException {
        Account account = null;
        try {
            account = accountProvider.findByOpenId(openId);
        } catch (Exception e) {
            throw new UsernameNotFoundException(e.getLocalizedMessage());
        }
        if (Objects.isNull(account)) {
            throw new UsernameNotFoundException("[" + openId + "] 对应的账户不存在");
        }
        return new SlardarUserDetails(account);
    }

    /**
     * 根据登录名称 获取 用户 details
     *
     * @param accountName
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String accountName) throws UsernameNotFoundException {
        return loadUserByAccount(accountName, Constants.REALM_EMPTY);
    }

    /**
     * load user by account and realm
     *
     * @param accountName
     * @param realm
     * @return
     * @throws UsernameNotFoundException
     */
    public SlardarUserDetails loadUserByAccount(String accountName, String realm) throws UsernameNotFoundException {
        Account account = null;
        try {
            account = accountProvider.findByName(accountName, realm);
        } catch (Exception e) {
            throw new UsernameNotFoundException("[" + accountName + "] 异常：" + e.getLocalizedMessage());
        }
        if (Objects.isNull(account)) {
            throw new UsernameNotFoundException("[" + accountName + "] 对应的账户不存在");
        }
        return new SlardarUserDetails(account);
    }
}
