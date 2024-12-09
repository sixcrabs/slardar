package cn.piesat.nj.slardar.example.impl;

import cn.hutool.core.util.RandomUtil;
import cn.piesat.v.slardar.core.provider.AccountProvider;
import cn.piesat.v.slardar.core.AccountStatus;
import cn.piesat.v.slardar.core.entity.Account;
import cn.piesat.v.slardar.core.entity.Authority;
import cn.piesat.v.slardar.core.entity.Role;
import cn.piesat.v.slardar.core.entity.UserProfile;
import com.google.common.collect.Lists;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
     * 模拟数据
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
        profile.setEmail("1075618458@qq.com");
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

        Account lisi = new Account().setName("lisi")
                .setPassword(ENCODER.encode("lisi123"));
        lisi.setRealm("master");
        lisi.setId(RandomUtil.randomString(8));
        lisi.setStatus(AccountStatus.accessible)
                .setUserProfile(profile2);

        ACCOUNTS.add(zhangsan);
        ACCOUNTS.add(lisi);
    }


    /**
     * find by openid
     *
     * @param openId
     * @return
     */
    @Override
    public Account findByOpenId(String openId) {
        System.out.println("openid: ".concat(openId));
        return null;
    }

    /**
     * find by name
     *
     * @param name
     * @param realm
     * @return
     */
    @Override
    public Account findByName(String name, String realm) {
        // 数据库查询
        return ACCOUNTS.stream().filter(account ->
                account.getName().equals(name)
        ).findFirst().orElse(null);
    }
}
