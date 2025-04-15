package cn.piesat.v.slardar.spi;

import cn.piesat.v.slardar.core.SlardarException;

import java.util.Collection;
import java.util.Map;

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
public interface SlardarKeyStore extends SlardarSpi {

    /**
     * set key
     *
     * @param key
     * @param val
     * @return
     */
    boolean set(String key, Object val);

    /**
     * set key if not exists
     *
     * @param key
     * @param val
     * @return
     */
    boolean setnx(String key, Object val);

    /**
     * set key with ttl
     *
     * @param key k
     * @param val value
     * @param ttl time to live in `seconds`
     * @return
     */
    boolean setex(String key, Object val, long ttl);

    /**
     * get key
     *
     * @param key
     * @param <T>
     * @return
     */
    <T> T get(String key);

    /**
     * has key
     *
     * @param key
     * @return
     */
    boolean has(String key);

    /**
     * remove key
     *
     * @param key
     */
    void remove(String key);

    /**
     * add listener
     *
     * @param key      data key
     * @param listener listener
     * @throws SlardarException
     */
    void addListener(String key, KeyEventListener listener) throws SlardarException;

    /**
     * get all keys
     *
     * @return
     */
    @Deprecated
    Collection<String> keys() throws SlardarException;

    /**
     * 以map 方式输出所有的 kv
     *
     * @return
     */
    @Deprecated
    Map<String, Object> toMap();

    interface KeyEventListener {

        /**
         * on event
         *
         * @param eventType event type
         * @param key       data key
         * @param val       data value
         */
        void onEvent(String eventType, String key, Object val);
    }

}
