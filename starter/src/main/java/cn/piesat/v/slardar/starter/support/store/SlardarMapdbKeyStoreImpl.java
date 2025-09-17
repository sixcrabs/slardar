package cn.piesat.v.slardar.starter.support.store;

import cn.piesat.v.misc.hutool.mini.ClassUtil;
import cn.piesat.v.misc.hutool.mini.StringUtil;
import cn.piesat.v.misc.hutool.mini.thread.ThreadUtil;
import cn.piesat.v.slardar.spi.SlardarKeyStore;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.starter.config.SlardarProperties;
import cn.piesat.v.timer.Timeout;
import cn.piesat.v.timer.TimerTask;
import cn.piesat.v.timer.cron.DateTimeUtil;
import cn.piesat.v.timer.job.TimerJobs;
import com.google.auto.service.AutoService;
import org.jetbrains.annotations.NotNull;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.piesat.v.slardar.starter.support.store.KeyStoreUtil.*;

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

    private static final Logger logger = LoggerFactory.getLogger(SlardarMapdbKeyStoreImpl.class);

    private static final String NAME = "mapdb";

    private DB db = null;

    /**
     * 用于记录expire key
     * key: setex 的key
     * value: 到期时间戳
     */
    private HTreeMap<String, Long> expireRecordMap = null;

    /**
     * 存储kv数据的map
     */
    private HTreeMap<String, String> dataMap = null;

    /**
     * 存储值的 class 类型
     */
    private HTreeMap<String, String> typedMap = null;

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
        // 根据 keystore配置 设置 mapdb
        SlardarProperties properties = context.getBeanIfAvailable(SlardarProperties.class);
        String type = properties.getKeyStore().getType();
        if (NAME.equalsIgnoreCase(type)) {
            logger.info("[keystore] 初始化存储 ....");
            Path path = StringUtil.isBlank(properties.getKeyStore().getUri()) ?
                    Paths.get(System.getProperty("user.home"), "keystore.db") : Paths.get(properties.getKeyStore().getUri());
            File dbFile = path.toFile();
            logger.info("[keystore] use data file: {}", dbFile.getPath());
            // 初始化 DB 对象
            db = DBMaker.fileDB(dbFile)
                    .checksumHeaderBypass()
                    .fileMmapEnableIfSupported()
                    .closeOnJvmShutdown()
                    .closeOnJvmShutdownWeakReference()
                    .allocateStartSize(1024 * 1024L)
                    .concurrencyScale(128)
                    .fileLockDisable()
                    .make();

            dataMap = db
                    .hashMap("data")
                    .keySerializer(Serializer.STRING)
                    .valueSerializer(Serializer.STRING)
                    .createOrOpen();
            expireRecordMap = db.hashMap("expired")
                    .keySerializer(Serializer.STRING)
                    .valueSerializer(Serializer.LONG)
                    .createOrOpen();

            typedMap = db.hashMap("typed")
                    .keySerializer(Serializer.STRING)
                    .valueSerializer(Serializer.STRING)
                    .createOrOpen();

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
            logger.trace("Scheduling MapDB commit task");
            scheduledThreadPool.scheduleWithFixedDelay(() -> {
                logger.trace("Committing to MapDB");
                db.commit();
            }, autosaveInterval, autosaveInterval, TimeUnit.SECONDS);
        }

    }

    /**
     * do destroy
     */
    @Override
    public void destroy() {
        super.destroy();
        if (db != null) {
            dataMap.close();
            expireRecordMap.close();
            db.commit();
            db.close();
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
    public boolean set(String key, @NotNull Object val) {
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
    @SuppressWarnings("unchecked")
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
        db.commit();
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
        List<String> keys = new ArrayList<>(dataMap.keySet());
        return StringUtil.isBlank(prefix) ? keys : keys.stream()
                .filter(key -> key.startsWith(prefix))
                .collect(Collectors.toList());
    }


    /**
     * 过期并trigger
     *
     * @param key
     */
    private void expireKey(String key) {
        notifyListeners(EXPIRED, key, get(key));
        dataMap.remove(key);
        expireRecordMap.remove(key);
        typedMap.remove(key);
        db.commit();
    }

}
