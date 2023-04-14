package cn.piesat.nj.slardar.starter.provider;

import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.SlardarAuthenticationProvider;
import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.SlardarUserDetails;
import cn.piesat.nj.slardar.starter.SlardarUserDetailsServiceImpl;
import cn.piesat.nj.slardar.starter.config.SlardarAuthenticationBeforeHandler;
import cn.piesat.nj.slardar.starter.support.SlardarAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

import static cn.piesat.nj.slardar.core.Constants.AUTH_TYPE_WX_APP;

/**
 * <p>
 *     TESTME:
 * 实现用户身份
 * - 密码认证
 * - openid 认证
 * - 可以扩展实现登录失败、特殊身份处理 等功能
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/26
 */
public class SlardarAuthenticationProviderImpl implements SlardarAuthenticationProvider {

    @Autowired
    private SlardarUserDetailsServiceImpl userDetailsService;

    private final SlardarContext context;

    private final PasswordEncoder passwordEncoder;

    private final SlardarAuthenticationBeforeHandler authenticationBeforeHandler;

    public SlardarAuthenticationProviderImpl(SlardarContext context) {
        this.context = context;
        this.passwordEncoder = context.getPwdEncoder();
        this.authenticationBeforeHandler = context.getBean(SlardarAuthenticationBeforeHandler.class);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SlardarAuthenticationToken authenticationToken = (SlardarAuthenticationToken) authentication;
        if (authenticationBeforeHandler != null) {
            try {
                authenticationBeforeHandler.before(authenticationToken);
            } catch (SlardarException e) {
                throw new AuthenticationServiceException(e.getLocalizedMessage());
            }
        }
        UserDetails userDetails;
        if (AUTH_TYPE_WX_APP.equals(authenticationToken.getAuthType())) {
            // openid 认证
            userDetails = userDetailsService.loadUserByOpenId(authenticationToken.getOpenId());
        } else {
            // 用户密码方式认证
            String accountName = authenticationToken.getAccountName();
            userDetails = userDetailsService.loadUserByAccount(accountName, authenticationToken.getRealm());
        }
        // 验证密码是否正确
        if (!Objects.isNull(userDetails)) {
            if (passwordEncoder.matches(authenticationToken.getPassword(), userDetails.getPassword())) {
                try {
                    return new SlardarAuthenticationToken(String.valueOf(authenticationToken.getPrincipal()), authenticationToken.getAuthType(),
                            (SlardarUserDetails) userDetails);
                } catch (Exception e) {
                    throw new AuthenticationServiceException(e.getLocalizedMessage());
                }
            } else {
                throw new AuthenticationServiceException(String.format("账户 [%s] 密码不匹配", userDetails.getUsername()));
            }
        } else {
            throw new UsernameNotFoundException("account not found");
        }
    }
}
