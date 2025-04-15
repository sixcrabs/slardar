package cn.piesat.v.slardar.starter.support.store;

import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.spi.SlardarKeyStore;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import com.google.auto.service.AutoService;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * 内存实现
 * </p>
 *
 * @author Alex
 * @since 2025/4/15
 */
@AutoService({SlardarKeyStore.class})
public class SlardarMemoryKeyStoreImpl implements SlardarKeyStore {

    private final Map<String, Object> store = new ConcurrentHashMap<>(1);

    @Override
    public boolean set(String key, Object val) {
        store.put(key, val);
        return true;
    }

    @Override
    public boolean setnx(String key, Object val) {
        return false;
    }

    @Override
    public boolean setex(String key, Object val, long ttl) {
        return false;
    }

    @Override
    public <T> T get(String key) {
        return null;
    }

    @Override
    public boolean has(String key) {
        return false;
    }

    @Override
    public void remove(String key) {

    }

    @Override
    public void addListener(String key, KeyEventListener listener) throws SlardarException {

    }

    @Override
    public Collection<String> keys() throws SlardarException {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> toMap() {
        return Collections.emptyMap();
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public void initialize(SlardarSpiContext context) {

    }
}
