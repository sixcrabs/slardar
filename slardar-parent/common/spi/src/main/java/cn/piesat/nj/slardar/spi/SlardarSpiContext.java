package cn.piesat.nj.slardar.spi;

import cn.piesat.nj.slardar.core.AccountProvider;
import cn.piesat.nj.slardar.core.AuditLogIngest;

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
     * get auditlog ingest
     *
     * @return
     */
    AuditLogIngest getAuditLogIngest();
}
