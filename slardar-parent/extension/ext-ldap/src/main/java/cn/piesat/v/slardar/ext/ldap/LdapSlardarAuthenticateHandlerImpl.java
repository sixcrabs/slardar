package cn.piesat.v.slardar.ext.ldap;

import cn.hutool.core.util.StrUtil;
import cn.piesat.v.slardar.core.entity.Account;
import cn.piesat.v.slardar.core.entity.UserProfile;
import cn.piesat.v.slardar.starter.SlardarUserDetails;
import cn.piesat.v.slardar.starter.authenticate.SlardarAuthentication;
import cn.piesat.v.slardar.starter.authenticate.handler.AbstractSlardarAuthenticateHandler;
import cn.piesat.v.slardar.starter.authenticate.handler.SlardarAuthenticateHandler;
import cn.piesat.v.slardar.starter.support.RequestWrapper;
import com.google.auto.service.AutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

import javax.naming.NamingEnumeration;
import javax.naming.directory.*;

/**
 * <p>
 * 使用 LDAP 进行身份认证
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/23
 */
@AutoService(SlardarAuthenticateHandler.class)
public class LdapSlardarAuthenticateHandlerImpl extends AbstractSlardarAuthenticateHandler {

    public static final Logger logger = LoggerFactory.getLogger(LdapSlardarAuthenticateHandlerImpl.class);

    private static final String NAME = "LDAP";

    /**
     * 认证处理类型 用于区分
     *
     * @return
     */
    @Override
    public String type() {
        return NAME;
    }

    /**
     * 处理认证请求
     *
     * @param requestWrapper
     * @return
     * @throws AuthenticationServiceException
     */
    @Override
    public SlardarAuthentication handleRequest(RequestWrapper requestWrapper) throws AuthenticationException {
        String username = requestWrapper.getRequestParams().get("username");
        String password = requestWrapper.getRequestParams().get("password");
        // 租户信息 默认为 master
        String realm = getRealm(requestWrapper);
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new AuthenticationServiceException("`username` and `password` should not be null");
        }
        return new SlardarAuthentication(username, NAME, null)
                .setRealm(realm)
                .setLoginDeviceType(requestWrapper.getLoginDeviceType())
                .setSessionId(requestWrapper.getSessionId())
                .setPassword(password);
    }


    /**
     * 子类实现
     * TESTME: 访问ldap 进行认证 和 用户身份同步
     *
     * @param authentication
     * @return
     */
    @Override
    protected SlardarAuthentication doAuthenticate0(SlardarAuthentication authentication) {
        LdapProperties properties = context.getBeanIfAvailable(LdapProperties.class);
        LdapUtils ldapUtils = new LdapUtils(properties.determineUrls(),
                StrUtil.format("cn={},{}", authentication.getAccountName(), properties.getBase()),
                authentication.getPassword(), properties.getBase());
        try {
            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(ldapUtils.getSearchScope());
            String filter = StrUtil.format("cn={}", authentication.getAccountName().trim(),
                    authentication.getCredentials().toString());
            NamingEnumeration<SearchResult> results = ldapUtils.getConnection().search(ldapUtils.getBaseDN(), filter, searchCtls);
            long recordCount = 0;
            while (null != results && results.hasMoreElements()) {
                SearchResult sr = results.nextElement();
                if (sr != null) {
                    // 同步ldap用户的信息
                    Account account = new Account();
                    logger.debug("LDAP User {} , name [{}] , NameInNamespace [{}]",
                            (++recordCount), sr.getName(), sr.getNameInNamespace());

                    Attributes attributes = sr.getAttributes();
                    account.setName(authentication.getAccountName())
                            .setRealm(authentication.getRealm());
                    account.setDeleted(0);
                    UserProfile userProfile = new UserProfile().setName(LdapUtils.getAttrStringValue(attributes.get("sn")))
                            .setTelephone(LdapUtils.getAttrStringValue(attributes.get("telephoneNumber")));
                    account.setUserProfile(userProfile);
                    authentication.setUserDetails(new SlardarUserDetails(account));
                }
            }
            if (recordCount == 0) {
                // not found
                throw new UsernameNotFoundException("用户不存在");

            }
        } catch (Exception e) {
            throw new AuthenticationServiceException(e.getLocalizedMessage());
        } finally {
            ldapUtils.close();
        }
        return authentication;
    }
}
