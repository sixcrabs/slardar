package cn.piesat.v.slardar.starter.support.store;

import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.spi.SlardarKeyStore;
import cn.piesat.v.timer.TimerManager;
import cn.piesat.v.timer.job.TimerJobs;

import java.security.Key;
import java.time.Duration;
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

    protected static final Map<String, List<KeyEventListener>> EVENT_LISTENERS = new ConcurrentHashMap<>(1);

    /**
     * 管理具有时效性的数据
     * 以 1s 为最小单位
     */
    protected static final TimerManager TIMER_MANAGER = new TimerManager(new TimerManager.TimerConfig().setTickDuration(1).setTicksPerWheel(64).setTimeUnit(TimeUnit.SECONDS));


    @Override
    public void addListener(String key, KeyEventListener listener) throws SlardarException {
        if (has(key)) {
            // key 存在时添加监听器
            EVENT_LISTENERS.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(listener);
        }
    }

    /**
     * add ttl timer
     *
     * @param key 设置 ttl 的key
     * @param ttl ttl 时间 单位秒
     * @return
     */
    protected boolean addTTLTimer(String key, long ttl) {
        if (ttl > 0) {
            // 清空已存在的 ttl timer
            TIMER_MANAGER.removeTimerJob(key);
            return TIMER_MANAGER.addTimerJob(TimerJobs.newDelayJob(key, Duration.ofSeconds(ttl), timeout -> {
                notifyListeners(EXPIRED, key, get(key));
                remove(key);
            }));
        }
        return false;
    }

    /**
     * remove all listeners fo key
     *
     * @param key
     */
    protected void removeListeners(String key) {
        List<KeyEventListener> listeners = getListeners(key);
        if (listeners != null) {
            listeners.clear();
        }
    }

    /**
     * 通知监听器
     *
     * @param eventType 事件类型 expired/removed/... 使用string 方便实现类扩展
     * @param key       事件关联的key
     * @param val       值，可选
     */
    protected void notifyListeners(String eventType, String key, Object val) {
        List<KeyEventListener> listeners = getListeners(key);
        if (listeners != null) {
            for (KeyEventListener listener : listeners) {
                listener.onEvent(eventType, key, val);
            }
        }
        if (REMOVED.equalsIgnoreCase(eventType)) {
            // key 被移除后 同时移除监听器和定时任务
            removeListeners(key);
            TIMER_MANAGER.removeTimerJob(key);
        }
    }

    /**
     * get event listeners
     *
     * @param key key
     * @return
     */
    protected List<KeyEventListener> getListeners(String key) {
        return EVENT_LISTENERS.get(key);
    }
}
