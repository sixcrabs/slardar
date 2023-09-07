package cn.piesat.nj.slardar.starter;

import cn.piesat.nj.slardar.core.AccountProvider;
import cn.piesat.nj.slardar.core.AuditLogIngest;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * <p>
 * context
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/14
 */
public class SlardarContext implements ApplicationContextAware {

    private ApplicationContext context;

    /**
     * 密码 encoder
     *
     * @return
     */
    public PasswordEncoder getPwdEncoder() {
        return getBeanOrDefault(PasswordEncoder.class, new BCryptPasswordEncoder());
    }


    /**
     * get real implement of {@link AuditLogIngest}
     *
     * @return
     */
    public AuditLogIngest getAuditLogIngest() {
        return getBeanIfAvailable(AuditLogIngest.class);
    }

    /**
     * get real implement of {@link AccountProvider}
     * @return
     */
    public AccountProvider getAccountProvider() {
        return getBeanIfAvailable(AccountProvider.class);
    }

    /**
     * get event manager
     *
     * @return
     */
    public SlardarEventManager getEventManager() {
        return getBeanIfAvailable(SlardarEventManager.class);
    }


    public <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    public <T> ObjectProvider<T> getBeanProvider(Class<T> clazz) {
        return context.getBeanProvider(clazz);
    }

    private <T> T getBeanOrDefault(Class<T> clazz, T defaultValue) {
        try {
            ObjectProvider<T> provider = getBeanProvider(clazz);
            return provider.getIfAvailable(() -> defaultValue);
        } catch (BeansException e) {
            return defaultValue;
        }
    }

    public  <T> T getBeanIfAvailable(Class<T> clazz) {
        ObjectProvider<T> provider = getBeanProvider(clazz);
        return provider.getIfAvailable();
    }


    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        context = ctx;
    }
}
