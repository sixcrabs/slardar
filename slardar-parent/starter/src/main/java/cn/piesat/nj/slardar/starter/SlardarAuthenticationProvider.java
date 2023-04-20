package cn.piesat.nj.slardar.starter;

import cn.piesat.nj.slardar.starter.support.SlardarAuthenticationToken;
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

    /**
     * supports or not
     * @param authentication
     *
     * @return
     */
    @Override
    default boolean supports(Class<?> authentication) {
        return SlardarAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
