package org.winterfell.slardar.starter;

import org.winterfell.slardar.core.provider.AccountProvider;
import org.winterfell.slardar.core.AuditLogIngest;
import org.winterfell.slardar.core.provider.ClientProvider;
import org.winterfell.slardar.spi.SlardarSpiContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;
import java.util.Map;

/**
 * <p>
 * spring impl of spi context
 * </p>
 *
 * @author alex
 * @version v1.0 2023/11/20
 */
public class SpringSlardarSpiContextImpl implements SlardarSpiContext, ApplicationContextAware {

    private ApplicationContext context;

    @Override
    public <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    /**
     * get beans by type
     *
     * @param clazz
     * @return
     */
    @Override
    public <T> Collection<T> getBeans(Class<T> clazz) {
        Map<String, T> beansOfType = context.getBeansOfType(clazz);
        return beansOfType.values();
    }

    @Override
    public <T> T getBeanIfAvailable(Class<T> clazz) {
        ObjectProvider<T> provider = getBeanProvider(clazz);
        return provider.getIfAvailable();
    }

    public <T> ObjectProvider<T> getBeanProvider(Class<T> clazz) {
        return context.getBeanProvider(clazz);
    }

    @Override
    public <T> T getBeanOrDefault(Class<T> clazz, T defaultValue) {
        try {
            ObjectProvider<T> provider = getBeanProvider(clazz);
            return provider.getIfAvailable(() -> defaultValue);
        } catch (BeansException e) {
            return defaultValue;
        }
    }

    @Override
    public AccountProvider getAccountProvider() {
        return getBeanIfAvailable(AccountProvider.class);
    }

    /**
     * get client provider impl
     *
     * @return
     */
    @Override
    public ClientProvider getClientProvider() {
        return getBeanIfAvailable(ClientProvider.class);
    }

    @Override
    public AuditLogIngest getAuditLogIngest() {
        return getBeanIfAvailable(AuditLogIngest.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        context = ctx;
    }
}
