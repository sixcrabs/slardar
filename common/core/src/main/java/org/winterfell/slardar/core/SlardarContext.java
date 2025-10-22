package org.winterfell.slardar.core;

import java.util.Collection;

/**
 * <p>
 * context for spi
 * </p>
 *
 * @author alex
 * @version v1.0 2023/11/20
 */
public interface SlardarContext {


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

}
