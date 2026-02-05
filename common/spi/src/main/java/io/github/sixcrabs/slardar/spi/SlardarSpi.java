package io.github.sixcrabs.slardar.spi;

import io.github.sixcrabs.slardar.core.SlardarContext;

/**
 * <p>
 * spi core interface
 * </p>
 *
 * @author alex
 * @version v1.0 2023/11/20
 */
public interface SlardarSpi {

    /**
     * 实现名称, 区分不同的 SPI 实现，必须
     * @return
     */
    String name();

    /**
     *  set context
     * @param context
     */
    void initialize(SlardarContext context);

    /**
     * do destroy
     */
    default void destroy() {}
}