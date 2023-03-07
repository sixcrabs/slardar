package cn.piesat.nj.slardar.starter.userdetails;

import cn.piesat.v.authx.security.domain.entity.UserProfile;
import cn.piesat.v.authx.security.domain.gateway.RealmGateway;
import cn.piesat.v.authx.security.domain.gateway.UserProfileGateway;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * <p>
 * .TODO
 * </p>
 *
 * @author Alex
 * @version v1.0 2022/12/14
 */
public class AuthxUserDetailsService {

    @Resource
    private UserProfileGateway userProfileGateway;

    @Resource
    private RealmGateway realmGateway;


    /**
     * 根据 openid 获取用户信息
     *
     * @param openId 唯一id
     * @return
     * @throws AuthenticationException
     */
    public UserDetails loadUserByOpenId(String openId) throws AuthenticationException {
        UserProfile userProfile = userProfileGateway.findByOpenId(openId);
        if (Objects.isNull(userProfile)) {
            throw new UsernameNotFoundException("[" + openId + "] 对应的用户不存在");
        }
        return new AuthxUserDetails(userProfile);
    }


    /**
     * TODO
     * 查询租户内的用户信息
     *
     * @param username
     * @param realmName 租户name 若为空 则默认是 master 租户
     * @return
     * @throws UsernameNotFoundException
     */
    public UserDetails loadUserByUsername(String username, String realmName) throws AuthenticationException {
        //
        realmGateway.getByName(realmName);


        return new AuthxUserDetails(new UserProfile());
    }
}
