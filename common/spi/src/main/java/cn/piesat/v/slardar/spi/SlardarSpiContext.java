package cn.piesat.v.slardar.spi;

import cn.piesat.v.slardar.core.provider.AccountProvider;
import cn.piesat.v.slardar.core.AuditLogIngest;
import cn.piesat.v.slardar.core.provider.ClientProvider;

import java.util.Collection;

/**
 * <p>
 * context for spi
 * </p>
 *
 * @author alex
 * @version v1.0 2023/11/20
 */
public interface SlardarSpiContext {


    /**
     * get bean
     *
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T getBean(Class<T> clazz);

    /**
     * get beans by type
     * @param clazz
     * @return
     * @param <T>
     */
    <T> Collection<T> getBeans(Class<T> clazz);

    /**
     * get bean if available
     *
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T getBeanIfAvailable(Class<T> clazz);

    /**
     * get bean or return default
     *
     * @param clazz
     * @param defaultValue
     * @param <T>
     * @return
     */
    <T> T getBeanOrDefault(Class<T> clazz, T defaultValue);

    /**
     * get account provider
     *
     * @return
     */
    AccountProvider getAccountProvider();

    /**
     * get client provider impl
     * @return
     */
    ClientProvider getClientProvider();

    /**
     * get auditlog ingest
     *
     * @return
     */
    AuditLogIngest getAuditLogIngest();


}
