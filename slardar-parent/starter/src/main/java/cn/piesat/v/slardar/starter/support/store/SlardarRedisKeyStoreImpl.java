package cn.piesat.v.slardar.starter.support.store;

import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.spi.SlardarKeyStore;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import com.google.auto.service.AutoService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * redis 实现
 * </p>
 *
 * @author Alex
 * @since 2025/4/15
 */
@AutoService(SlardarKeyStore.class)
public class SlardarRedisKeyStoreImpl implements SlardarKeyStore {
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
     * 返回类似的 key 集合
     *
     * @param prefix 前缀 eg: `user_` 即返回所有以此开头的 key
     * @return
     */
    @Override
    public List<String> keys(String prefix) {
        return Collections.emptyList();
    }

    /**
     * add listener
     *
     * @param key      data key
     * @param listener listener
     * @throws SlardarException
     */
    @Override
    public void addListener(String key, KeyEventListener listener) throws SlardarException {

    }

    /**
     * 实现名称, 区分不同的 SPI 实现，必须
     *
     * @return
     */
    @Override
    public String name() {
        return "redis";
    }

    /**
     * set context
     *
     * @param context
     */
    @Override
    public void initialize(SlardarSpiContext context) {
        // TODO: 根据keyStore 配置 连接 redis

    }
}
