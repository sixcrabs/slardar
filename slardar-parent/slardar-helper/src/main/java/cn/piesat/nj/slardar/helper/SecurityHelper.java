package cn.piesat.nj.slardar.helper;

import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 在业务第三方应用中使用此类可以获取到当前从网关传递的 用户信息
 * </p>
 *
 * @author alex
 * @version v1.0 2022/2/18
 */
public class SecurityHelper {


    private static final ThreadLocal<SecurityContext> contextHolder = new InheritableThreadLocal<>();


    /**
     * is admin role
     *
     * @return
     */
    public static boolean isSysAdmin() {
        return getContext().isSysAdmin();
    }

    public static String getUserId() {
        return getContext().getUserId();
    }

    public static String getUserLoginName() {
        return getContext().getUserLoginName();
    }

    public static String getUserRealName() {
        return getContext().getUserRealName();
    }

    public static String getAttribute(String name) {
        return getContext().getAttributes().get(name.toLowerCase());
    }

    // ~ context Methods
    // ========================================================================================================

    static void clearContext() {
        contextHolder.remove();
    }

    public static SecurityContext getContext() {
        SecurityContext ctx = contextHolder.get();

        if (ctx == null) {
            ctx = createEmptyContext();
            contextHolder.set(ctx);
        }

        return ctx;
    }

    static void setContext(SecurityContext context) {
        Assert.notNull(context, "Only non-null SecurityContext instances are permitted");
        contextHolder.set(context);
    }

    static SecurityContext createEmptyContext() {
        return new SecurityContext();
    }

    public static class SecurityContext implements Serializable {

        private boolean sysAdmin;

        private String userId;

        private String userLoginName;

        private String userRealName;

        private Map<String, String> attributes = Collections.emptyMap();

        public SecurityContext(boolean isSysAdmin, String userId, String userLoginName) {
            this.sysAdmin = isSysAdmin;
            this.userId = userId;
            this.userLoginName = userLoginName;
        }

        public SecurityContext() {
        }

        public SecurityContext setUserRealName(String userRealName) {
            this.userRealName = userRealName;
            return this;
        }

        public SecurityContext addAttribute(String name, String value) {
            if (attributes.isEmpty()) {
                attributes = new HashMap<>(1);
            }
            attributes.put(name, value);
            return this;
        }


        public Map<String, String> getAttributes() {
            return attributes;
        }

        public SecurityContext setAttributes(Map<String, String> attributes) {
            this.attributes = attributes;
            return this;
        }

        public String getUserRealName() {
            return userRealName;
        }

        public boolean isSysAdmin() {
            return sysAdmin;
        }

        public SecurityContext setSysAdmin(boolean sysAdmin) {
            this.sysAdmin = sysAdmin;
            return this;
        }

        public String getUserId() {
            return userId;
        }

        public SecurityContext setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public String getUserLoginName() {
            return userLoginName;
        }

        public SecurityContext setUserLoginName(String userLoginName) {
            this.userLoginName = userLoginName;
            return this;
        }
    }
}
