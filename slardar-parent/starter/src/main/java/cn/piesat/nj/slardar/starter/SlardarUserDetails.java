package cn.piesat.nj.slardar.starter;

import cn.piesat.nj.slardar.core.AccountStatus;
import cn.piesat.nj.slardar.core.entity.Account;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * <p>
 *     TODO
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
        // TODO: 组织权限信息
        return null;
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
