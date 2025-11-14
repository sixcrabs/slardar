package org.winterfell.slardar.core;

import org.winterfell.slardar.core.domain.Account;
import org.winterfell.slardar.core.domain.Role;
import org.winterfell.slardar.core.domain.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 设置、获取当前用户信息等
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/4/3
 */
public final class SlardarSecurityHelper {

    private static final Logger log = LoggerFactory.getLogger(SlardarSecurityHelper.class);

    private static final ThreadLocal<SecurityContext> CONTEXT_HOLDER = new InheritableThreadLocal<>();

    private SlardarSecurityHelper() {
    }

    /**
     * get 登录账户名
     *
     * @return
     */
    public static String getCurrentUsername() {
        Account account = getAccount();
        return Objects.isNull(account) ? null : account.getName();
    }

    /**
     * account
     *
     * @return
     */
    public static Account getAccount() {
        return getContext().getAccount();
    }

    /**
     * user profile
     *
     * @return
     */
    public static UserProfile getUserProfile() {
        return getContext().getUserProfile();
    }

    /**
     * get attr
     *
     * @param name
     * @return
     */
    public static Object getAttribute(String name) {
        Map<String, Object> attributes = getUserProfile().getAttributes();
        return attributes.getOrDefault(name, null);
    }

    /**
     * get security context
     *
     * @return
     */
    public static SecurityContext getContext() {
        SecurityContext ctx = CONTEXT_HOLDER.get();
        if (ctx == null) {
            ctx = createEmptyContext();
            CONTEXT_HOLDER.set(ctx);
        }
        return ctx;
    }

    static void setContext(SecurityContext context) {
        notNull(context, "Only non-null SecurityContext instances are permitted");
        CONTEXT_HOLDER.set(context);
    }

    static SecurityContext createEmptyContext() {
        return new SecurityContext();
    }

    /**
     * 角色是否匹配
     *
     * @param roleNames
     * @return
     */
    public static boolean isRoleMatches(String... roleNames) {
        try {
            List<Role> roles =
                    getAccount().getUserProfile().getRoles();
            return roles.stream().anyMatch(role ->
                    Arrays.asList(roleNames).contains(role.getName())
            );
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return false;
        }
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void clear() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * context
     */
    public static class SecurityContext implements Serializable {

        private Account account;

        private UserProfile userProfile;

        private boolean authenticated = false;

        public boolean isAuthenticated() {
            return authenticated;
        }

        public void setAuthenticated(boolean authenticated) {
            this.authenticated = authenticated;
        }

        public Account getAccount() {
            return account;
        }

        public SecurityContext setAccount(Account account) {
            this.account = account;
            return this;
        }

        public UserProfile getUserProfile() {
            return userProfile;
        }

        public SecurityContext setUserProfile(UserProfile userProfile) {
            this.userProfile = userProfile;
            return this;
        }
    }
}