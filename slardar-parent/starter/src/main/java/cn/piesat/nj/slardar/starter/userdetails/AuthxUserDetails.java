package cn.piesat.nj.slardar.starter.userdetails;

import cn.piesat.v.authx.security.core.UserAccessStatus;
import cn.piesat.v.authx.security.domain.entity.UserProfile;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * <p>
 * .TODO
 * </p>
 *
 * @author Alex
 * @version v1.0 2022/12/14
 */
@Data
public class AuthxUserDetails implements UserDetails {

    private final UserProfile userProfile;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return this.userProfile.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return !UserAccessStatus.expired.equals(this.userProfile.getStatus());
    }

    @Override
    public boolean isAccountNonLocked() {
        return !UserAccessStatus.locked.equals(this.userProfile.getStatus());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return UserAccessStatus.accessible.equals(this.userProfile.getStatus());
    }
}
