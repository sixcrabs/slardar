package cn.piesat.v.slardar.starter.support.store;

import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.spi.SlardarKeyStore;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import com.google.auto.service.AutoService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <p>
 * 内存实现
 * </p>
 *
 * @author Alex
 * @since 2025/4/15
 */
@AutoService({SlardarKeyStore.class})
public class SlardarMemoryKeyStoreImpl extends AbstractKeyStoreImpl {

    public static final String NAME = "memory";

    private final Map<String, Object> map = new ConcurrentHashMap<>(1);

    /**
     * set key
     *
     * @param key
     * @param val
     * @return
     */
    @Override
    public boolean set(String key, Object val) {
        map.put(key, val);
        return true;
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
        if (has(key)) {
            return false;
        }
        return set(key, val);
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
        boolean b = set(key, val);
        if (b) {
            return addTTLTimer(key, ttl);
        }
        return false;
    }

    /**
     * get key
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key) {
        return (T) map.get(key);
    }

    /**
     * has key
     *
     * @param key
     * @return
     */
    @Override
    public boolean has(String key) {
        return map.containsKey(key);
    }

    /**
     * remove key
     *
     * @param key
     */
    @Override
    public void remove(String key) {
        map.remove(key);
        notifyListeners(REMOVED, key, null);
    }

    /**
     * 返回类似的 key 集合
     *
     * @param prefix 前缀 eg: `user_` 即返回所有以此开头的 key
     * @return
     */
    @Override
    public List<String> keys(String prefix) {
        return map.keySet().stream().filter(key -> key.startsWith(prefix)).collect(Collectors.toList());
    }

    /**
     * 实现名称, 区分不同的 SPI 实现，必须
     *
     * @return
     */
    @Override
    public String name() {
        return NAME;
    }

    /**
     * set context
     *
     * @param context
     */
    @Override
    public void initialize(SlardarSpiContext context) {
        // do nothing
    }

    /**
     * do destroy
     */
    @Override
    public void destroy() {
        super.destroy();
        map.clear();
    }
}
