package org.winterfell.slardar.spi;

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
    void initialize(SlardarSpiContext context);

    /**
     * do destroy
     */
    default void destroy() {}
}
