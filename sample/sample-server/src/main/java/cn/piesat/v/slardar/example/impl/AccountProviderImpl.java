package cn.piesat.v.slardar.example.impl;

import org.winterfell.misc.hutool.mini.RandomUtil;
import org.winterfell.slardar.core.SlardarException;
import org.winterfell.slardar.starter.provider.AccountProvider;
import org.winterfell.slardar.core.AccountStatus;
import org.winterfell.slardar.core.domain.Account;
import org.winterfell.slardar.core.domain.Authority;
import org.winterfell.slardar.core.domain.Role;
import org.winterfell.slardar.core.domain.UserProfile;
import com.google.common.collect.Lists;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/16
 */
@Component
public class AccountProviderImpl implements AccountProvider {

    /**
     * 模拟用户数据
     */
    private static final List<UserProfile> USER_PROFILES = Lists.newArrayList();

    private static final List<Account> ACCOUNTS = Lists.newArrayList();

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    static {

        UserProfile profile = new UserProfile()
                .setAddress(RandomUtil.randomString(5))
                .setName("张三")
                .setTelephone("13456789765");
        profile.setRealm("master");
        profile.setDeleted(0);
        profile.setId(RandomUtil.randomString(8));
        profile.setEmail("1075xxxxx@qq.com");
        profile.setRoles(Lists.newArrayList(new Role().setName("NORMAL_USER")));
        profile.setAuthorities(Lists.newArrayList(new Authority().setContent("READ_URL")));

        UserProfile profile2 = new UserProfile()
                .setAddress(RandomUtil.randomString(5))
                .setName("李四")
                .setTelephone("13756789765");
        profile2.setRealm("master");
        profile2.setDeleted(0);
        profile2.setId(RandomUtil.randomString(8));
        profile2.setRoles(Lists.newArrayList(new Role().setName("NORMAL_USER"),
                new Role().setName("ADMIN")));

        USER_PROFILES.add(profile);
        USER_PROFILES.add(profile2);

        Account zhangsan = new Account().setName("zhangsan")
                .setPassword(ENCODER.encode("zhangsan123"));
        zhangsan.setRealm("master");
        zhangsan.setId(RandomUtil.randomString(8));
        zhangsan.setStatus(AccountStatus.accessible)
                .setUserProfile(profile);
        // 口令过期剩余天数
        zhangsan.setPwdValidRemainDays(5);
        zhangsan.setExpireAt(LocalDateTime.now().plus(Duration.ofDays(20)));

        Account lisi = new Account().setName("lisi")
                .setPassword(ENCODER.encode("lisi123"));
        lisi.setRealm("master");
        lisi.setId(RandomUtil.randomString(8));
        lisi.setStatus(AccountStatus.accessible)
                .setUserProfile(profile2);
        lisi.setExpireAt(LocalDateTime.now().plus(Duration.ofDays(10)));

        ACCOUNTS.add(zhangsan);
        ACCOUNTS.add(lisi);
    }

    /**
     * find by name
     *
     * @param name
     * @param realm
     * @return
     */
    @Override
    public Account findByName(String name, String realm) throws SlardarException {
        // 查询账户
        return ACCOUNTS.stream().filter(account ->
                account.getName().equals(name)
        ).findFirst().orElse(null);
    }

    /**
     * find by openid
     *
     * @param openId
     * @return
     */
    @Override
    public Account findByOpenId(String openId) throws SlardarException {
        throw new SlardarException("Unsupported");
    }
}
