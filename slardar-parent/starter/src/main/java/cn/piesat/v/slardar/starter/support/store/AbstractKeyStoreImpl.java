package cn.piesat.v.slardar.starter.support.store;

import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.spi.SlardarKeyStore;
import cn.piesat.v.timer.TimerManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/4/15
 */
public abstract class AbstractKeyStoreImpl implements SlardarKeyStore {

    protected static final Map<String, List<KeyEventListener>> EVENT_LISTENERS =  new ConcurrentHashMap<>(1);
    /**
     * 用于ttl类的数据
     * 以 1s 为最小单位
     */
    protected static final TimerManager TTL_MANAGER = new TimerManager(new TimerManager.TimerConfig().setTickDuration(1).setTicksPerWheel(64).setTimeUnit(TimeUnit.SECONDS));


    @Override
    public void addListener(String key, KeyEventListener listener) throws SlardarException {
        // TODO:
        if (has(key)) {
            // key 存在时添加监听器
            EVENT_LISTENERS.computeIfAbsent(key, k -> {
                // 创建一个空的监听器列表
                return new CopyOnWriteArrayList<>();
            }).add(listener);
        }

    }
}
