package org.winterfell.slardar.starter.support.store;

import cn.piesat.v.misc.hutool.mini.StringUtil;
import cn.piesat.v.misc.hutool.mini.thread.ThreadUtil;
import org.winterfell.slardar.spi.SlardarKeyStore;
import org.winterfell.slardar.spi.SlardarSpiContext;
import org.winterfell.slardar.starter.config.SlardarProperties;
import cn.piesat.v.timer.cron.DateTimeUtil;
import cn.piesat.v.timer.job.TimerJobs;
import com.google.auto.service.AutoService;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.winterfell.slardar.starter.support.store.KeyStoreUtil.*;

/**
 * <p>
 * 使用 h2 mvstore 实现key存储
 * </p>
 *
 * @author Alex
 * @since 2025/5/14
 */
@AutoService(SlardarKeyStore.class)
public class SlardarMVStoreKeyStoreImpl extends AbstractKeyStoreImpl {

    private static final Logger logger = LoggerFactory.getLogger(SlardarMVStoreKeyStoreImpl.class);

    public static final String NAME = "mvstore";

    private MVStore mvStore = null;

    private MVMap<String, String> dataMap = null;
    private MVMap<String, String> typedMap = null;
    /**
     * 用于记录expire key
     * key: setex 的key
     * value: 到期时间戳
     */
    private MVMap<String, Long> expireRecordMap = null;

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
        SlardarProperties properties = context.getBeanIfAvailable(SlardarProperties.class);
        String type = properties.getKeyStore().getType();
        if (NAME.equalsIgnoreCase(type)) {
            Path storePath = StringUtil.isBlank(properties.getKeyStore().getUri()) ?
                    Paths.get(System.getProperty("user.home"), "h2_keystore.db") : Paths.get(properties.getKeyStore().getUri());
            logger.info("Initializing H2 mv store to {}", storePath);
//            if (!Files.exists(storePath)) {
//                try {
//                    Files.createFile(storePath);
//                } catch (IOException ex) {
//                    throw new IllegalArgumentException("Error creating " + storePath + " file", ex);
//                }
//            }
            // 创建或打开一个 MVStore 参数为文件路径，如果文件不存在则创建，如果为 null 则创建内存模式的 store
            mvStore = new MVStore.Builder()
                    .fileName(storePath.toString())
                    .compress() // 可以选择开启数据压缩
                    .autoCommitDisabled()
                    .open();

            dataMap = mvStore.openMap("data");
            typedMap = mvStore.openMap("typed");
            expireRecordMap = mvStore.openMap("expired");

            ThreadUtil.execute(() -> {
                long now = DateTimeUtil.toTimeStamp();
                expireRecordMap.forEach((key, value) -> {
                    if (value <= now) {
                        expireKey(key);
                    } else {
                        // 加入计时器
                        TIMER_MANAGER.addTimerJob(
                                TimerJobs.newDelayJob(key, Duration.ofMillis(value - now), timeout -> {
                                    expireKey(key);
                                })
                        );
                    }
                });
            });
            logger.trace("Scheduling H2 commit task");
            scheduledThreadPool.scheduleWithFixedDelay(() -> {
                logger.trace("Committing to H2");
                mvStore.commit();
            }, autosaveInterval, autosaveInterval, TimeUnit.SECONDS);

        }
    }

    /**
     * do destroy
     */
    @Override
    public void destroy() {
        super.destroy();
        if (mvStore != null) {
            mvStore.commit();
            mvStore.close();
        }
    }

    /**
     * set key
     *
     * @param key
     * @param val
     * @return
     */
    @Override
    public boolean set(String key, Object val) {
        Class<?> valClass = val.getClass();
        typedMap.put(key, valClass.getName());
        dataMap.put(key, stringify(val));
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
            logger.trace("key [{}] already exists", key);
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
            boolean added = addTTLTimer(key, ttl, timeout -> expireKey(key));
            if (added) {
                // 收集到期时间
                expireRecordMap.put(key, DateTimeUtil.toTimeStamp() + ttl * 1000L);
            }
            return added;
        }
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
        if (has(key)) {
            String strVal = dataMap.get(key);
            String className = typedMap.get(key);
            try {
                Class<?> clazz = Class.forName(className);
                if (isPrimitive(clazz)) {
                    return (T) stringToPrimitive(strVal, clazz);
                } else {
                    // TODO: 需要考虑 List / Map 等情况
                    return (T) GSON.fromJson(strVal, clazz);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
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
        return dataMap.containsKey(key);
    }

    /**
     * remove key
     *
     * @param key
     */
    @Override
    public void remove(String key) {
        dataMap.remove(key);
        expireRecordMap.remove(key);
        typedMap.remove(key);
        TIMER_MANAGER.removeTimerJob(key);
    }

    /**
     * 返回类似的 key 集合
     *
     * @param prefix 前缀 eg: `user_` 即返回所有以此开头的 key
     * @return
     */
    @Override
    public List<String> keys(String prefix) {
        List<String> keys = dataMap.keyList();
        return StringUtil.isBlank(prefix) ? keys : keys.stream().filter(key -> key.startsWith(prefix)).collect(Collectors.toList());
    }

    private void expireKey(String key) {
        notifyListeners(EXPIRED, key, get(key));
        dataMap.remove(key);
        expireRecordMap.remove(key);
        typedMap.remove(key);
        mvStore.commit();
    }

}
