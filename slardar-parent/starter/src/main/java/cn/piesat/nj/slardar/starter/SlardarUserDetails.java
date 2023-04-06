package cn.piesat.nj.slardar.starter;

import cn.piesat.nj.slardar.core.AccountStatus;
import cn.piesat.nj.slardar.core.entity.Account;
import cn.piesat.nj.slardar.core.entity.Authority;
import cn.piesat.nj.slardar.core.entity.Role;
import cn.piesat.nj.slardar.core.entity.UserProfile;
import com.google.common.collect.Lists;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

import static cn.piesat.nj.slardar.starter.support.SecUtil.ROLE_NAME_PREFIX;

/**
 * <p>
 * 重新定义的用户 details
 * </p>
 *
 * @author Alex
 * @version v1.0 2022/12/14
 */
@Data
public class SlardarUserDetails implements UserDetails {

    /**
     * 账号实体
     */
    private final Account account;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = Lists.newArrayList();
        UserProfile userProfile = account.getUserProfile();
        // 添加所有的 role (自动添加 'ROLE_')
        List<Role> roles = userProfile.getRoles();
        if (roles != null && roles.size() > 0) {
            roles.forEach(role -> authorities.add((GrantedAuthority) () -> ROLE_NAME_PREFIX.concat(role.getName())));
        }
        // 添加所有的 authority
        List<Authority> list = userProfile.getAuthorities();
        if (list != null && list.size() > 0) {
            list.forEach(authority -> authorities.add((GrantedAuthority) authority::getContent));
        }
///        AuthorityUtils.commaSeparatedStringToAuthorityList();
        return authorities.size() > 0 ? authorities : null;
    }

    @Override
    public String getPassword() {
        return this.account.getPassword();
    }

    @Override
    public String getUsername() {
        return this.account.getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return !AccountStatus.expired.equals(this.account.getStatus());
    }

    @Override
    public boolean isAccountNonLocked() {
        return !AccountStatus.locked.equals(this.account.getStatus());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return AccountStatus.accessible.equals(this.account.getStatus());
    }
}
