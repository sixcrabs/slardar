package cn.piesat.nj.slardar.starter;

import cn.piesat.v.authx.security.infrastructure.spring.support.AuthxAuthentication;
import org.springframework.security.authentication.AuthenticationProvider;

/**
 * <p>
 * 实现身份认证
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/26
 */
public interface SlardarAuthenticationProvider extends AuthenticationProvider {

    @Override
    default boolean supports(Class<?> authentication) {
        return AuthxAuthentication.class.isAssignableFrom(authentication);
    }
}
