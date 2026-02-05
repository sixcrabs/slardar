package io.github.sixcrabs.slardar.starter.authenticate;

import io.github.sixcrabs.slardar.starter.provider.AccountProvider;
import io.github.sixcrabs.slardar.core.Constants;
import io.github.sixcrabs.slardar.core.domain.Account;
import io.github.sixcrabs.slardar.core.SlardarContext;
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

    public SlardarUserDetailsServiceImpl(SlardarContext slardarContext) {
        this.accountProvider = slardarContext.getBeanIfAvailable(AccountProvider.class);
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