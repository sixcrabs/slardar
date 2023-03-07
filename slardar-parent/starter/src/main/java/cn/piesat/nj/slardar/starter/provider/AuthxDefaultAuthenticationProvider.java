package cn.piesat.nj.slardar.starter.provider;

import cn.piesat.v.authx.security.domain.gateway.UserProfileGateway;
import cn.piesat.v.authx.security.infrastructure.spring.AuthxAuthenticationProvider;
import cn.piesat.v.authx.security.infrastructure.spring.support.AuthxAuthentication;
import cn.piesat.v.authx.security.infrastructure.spring.userdetails.AuthxUserDetails;
import cn.piesat.v.authx.security.infrastructure.spring.userdetails.AuthxUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.util.Objects;

import static cn.piesat.v.authx.security.infrastructure.spring.support.SecUtil.AUTH_TYPE_WXAPP;

/**
 * <p>
 * 实现用户身份
 * - 密码认证
 * - openid 认证
 * - 可以扩展实现登录失败、特殊身份处理 等功能
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/26
 */
public class AuthxDefaultAuthenticationProvider implements AuthxAuthenticationProvider {

    @Autowired
    private AuthxUserDetailsService userDetailsService;

    @Resource
    private UserProfileGateway userProfileGateway;

    private final PasswordEncoder passwordEncoder;

    public AuthxDefaultAuthenticationProvider(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        AuthxAuthentication authxAuthentication = (AuthxAuthentication) authentication;
        UserDetails userDetails;
        if (AUTH_TYPE_WXAPP.equals(authxAuthentication.getAuthType())) {
            // openid 认证
            userDetails = userDetailsService.loadUserByOpenId(String.valueOf(authentication.getPrincipal()));
        } else {
            // 用户密码方式认证
            Object username = authentication.getPrincipal();
            userDetails = userDetailsService.loadUserByUsername(String.valueOf(username), authxAuthentication.getRealm());
        }
        if (!Objects.isNull(userDetails)) {
            // 验证密码是否正确
            if (passwordEncoder.matches(authentication.getCredentials().toString(), userDetails.getPassword())) {
                try {
                    return new AuthxAuthentication(authentication.getPrincipal(), (AuthxUserDetails) userDetails);
                } catch (Exception e) {
                    throw new AuthenticationServiceException(e.getLocalizedMessage());
                }
            } else {
                throw new AuthenticationServiceException("password is invalid");
            }

        } else {
            throw new UsernameNotFoundException("user not found");
        }
    }
}
