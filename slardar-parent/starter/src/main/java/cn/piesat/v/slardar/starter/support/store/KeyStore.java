package cn.piesat.v.slardar.starter.support.store;

import cn.piesat.v.slardar.spi.SlardarSpi;

/**
 * <p>
 * 存储、获取kv 等操作，支持
 * - memory
 * - redis
 * - lmdbjava
 * </p>
 *
 * @author Alex
 * @since 2025/3/28
 */
public interface KeyStore extends SlardarSpi {
}
