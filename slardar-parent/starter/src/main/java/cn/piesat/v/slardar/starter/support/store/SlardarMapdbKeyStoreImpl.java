package cn.piesat.v.slardar.starter.support.store;

import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.spi.SlardarKeyStore;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import com.google.auto.service.AutoService;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * <p>
 * mapdb 实现
 * </p>
 *
 * @author Alex
 * @since 2025/4/15
 */
@AutoService(SlardarKeyStore.class)
public class SlardarMapdbKeyStoreImpl extends AbstractKeyStoreImpl {

    /**
     * set key
     *
     * @param key
     * @param val
     * @return
     */
    @Override
    public boolean set(String key, Object val) {
        return false;
    }

    /**
     * set key if not exists
     *
     * @param key
     * @param val
     * @return
     */
    @Override
    public boolean setnx(String key, Object val) {
        return false;
    }

    /**
     * set key with ttl
     *
     * @param key k
     * @param val value
     * @param ttl time to live in `seconds`
     * @return
     */
    @Override
    public boolean setex(String key, Object val, long ttl) {
        return false;
    }

    /**
     * get key
     *
     * @param key
     * @return
     */
    @Override
    public <T> T get(String key) {
        return null;
    }

    /**
     * has key
     *
     * @param key
     * @return
     */
    @Override
    public boolean has(String key) {
        return false;
    }

    /**
     * remove key
     *
     * @param key
     */
    @Override
    public void remove(String key) {

    }

    /**
     * get all keys
     *
     * @return
     */
    @Override
    public Collection<String> keys() throws SlardarException {
        return Collections.emptyList();
    }

    /**
     * 以map 方式输出所有的 kv
     *
     * @return
     */
    @Override
    public Map<String, Object> toMap() {
        return Collections.emptyMap();
    }

    /**
     * 实现名称, 区分不同的 SPI 实现，必须
     *
     * @return
     */
    @Override
    public String name() {
        return "mapdb";
    }

    /**
     * set context
     *
     * @param context
     */
    @Override
    public void initialize(SlardarSpiContext context) {
        // TODO 根据 keystore配置 设置 mapdb

    }
}
