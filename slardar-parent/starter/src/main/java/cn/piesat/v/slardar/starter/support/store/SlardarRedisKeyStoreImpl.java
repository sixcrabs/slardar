package cn.piesat.v.slardar.starter.support.store;

import cn.piesat.v.misc.hutool.mini.ReUtil;
import cn.piesat.v.misc.hutool.mini.StringUtil;
import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.spi.SlardarKeyStore;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.starter.config.SlardarProperties;
import com.google.auto.service.AutoService;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import static cn.piesat.v.slardar.starter.support.store.KeyStoreUtil.stringify;
import static jdk.nashorn.internal.runtime.JSType.isPrimitive;

/**
 * <p>
 * redis 实现
 * </p>
 *
 * @author Alex
 * @since 2025/4/15
 */
@AutoService(SlardarKeyStore.class)
public class SlardarRedisKeyStoreImpl extends AbstractKeyStoreImpl {

    private static final Logger logger = LoggerFactory.getLogger(SlardarRedisKeyStoreImpl.class);

    public static final String NAME = "redis";

    private RedisClient redisClient = null;

    private GenericObjectPool<StatefulRedisConnection> connectionPool;

    private static final int POOL_MAX_SIZE = Runtime.getRuntime().availableProcessors();

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
            String uri = properties.getKeyStore().getUri();
            if (StringUtil.isBlank(uri)) {
                logger.warn("Redis uri is empty, will use default value");
                uri = "redis://localhost/1";
            }
            String encrypted = KeyStoreUtil.getEncryptedFromRedisUri(uri);
            if (!encrypted.isEmpty()) {
                logger.warn("redis uri contains encrypted: {}", encrypted);
            }
            redisClient = RedisClient.create(StringUtil.isBlank(encrypted) ? uri : uri.replace(encrypted, KeyStoreUtil.decrypt(encrypted)));
            createRedisConnectionPool();
        }

    }

    /**
     * create redis connection pool
     */
    private void createRedisConnectionPool() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(POOL_MAX_SIZE * 2);
        poolConfig.setMaxIdle(POOL_MAX_SIZE);
        connectionPool = new GenericObjectPool<>(new RedisPooledFactory(redisClient), poolConfig);
        for (int i = 0; i < POOL_MAX_SIZE; i++) {
            try {
                connectionPool.addObject();
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage());
            }
        }
        logger.info("pool connected to redis");
    }

    /**
     * do destroy
     */
    @Override
    public void destroy() {
        super.destroy();
        try {
            if (connectionPool != null) {
                connectionPool.close();
            }
        } catch (Exception e) {
            logger.warn("Failed to close connection pool", e);
        } finally {
            if (redisClient != null) {
                redisClient.shutdown();
            }
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
        return Boolean.TRUE.equals(executeWithConnection(conn -> {
            RedisCommands commands = conn.sync();
            if (isPrimitive(val)) {
                commands.set(key, val);
            } else {
                commands.set(key, stringify(val));
            }
            return true;
        }));
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
        return Boolean.TRUE.equals(executeWithConnection(conn -> {
            RedisCommands commands = conn.sync();
            return isPrimitive(val) ? commands.setnx(key, val) : commands.setnx(key, stringify(val));
        }));
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
        return Boolean.TRUE.equals(executeWithConnection(conn -> {
            RedisCommands commands = conn.sync();
            String reply = isPrimitive(val) ? commands.setex(key, ttl, val) : commands.setex(key, ttl, stringify(val));
            return StringUtil.isNotBlank(reply);
        }));
    }

    /**
     * TODO:
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
        return Boolean.TRUE.equals(executeWithConnection(conn -> {
            RedisCommands commands = conn.sync();
            return commands.exists(key) > 0L;
        }));
    }


    /**
     * 当前key 的ttl时间 单位秒
     * 默认都返回 -1L， 只有 redis 才支持
     *
     * @param key
     * @return
     */
    @Override
    public long ttl(String key) {
        executeWithConnection(conn -> {
            RedisCommands commands = conn.sync();
            return commands.ttl(key);
        });
        return 0L;
    }

    /**
     * remove key
     *
     * @param key
     */
    @Override
    public void remove(String key) {
        boolean succeed = executeWithConnection(conn -> {
            RedisCommands commands = conn.sync();
            return commands.del(key) > 0L;
        });
        if (succeed) {
            notifyListeners(REMOVED, key, null);
        }
    }

    /**
     * 返回类似的 key 集合
     *
     * @param prefix 前缀 eg: `user_` 即返回所有以此开头的 key
     * @return
     */
    @Override
    public List<String> keys(String prefix) {
        return executeWithConnection(conn -> {
            RedisCommands commands = conn.sync();
            return (List<String>) commands.keys(prefix.concat(".*"));
        });
    }

    private <T> T executeWithConnection(Function<StatefulRedisConnection, T> action) {
        StatefulRedisConnection connection = null;
        try {
            connection = connectionPool.borrowObject();
            return action.apply(connection);
        } catch (Exception e) {
            logger.error("Redis operation failed", e);
            return null;
        } finally {
            if (connection != null) {
                connectionPool.returnObject(connection);
            }
        }
    }

    private static class RedisPooledFactory extends BasePooledObjectFactory<StatefulRedisConnection> {
        private final RedisClient redisClient;

        public RedisPooledFactory(RedisClient redisClient) {
            this.redisClient = redisClient;
        }

        @Override
        public StatefulRedisConnection create() throws Exception {
            return redisClient.connect();
        }

        @Override
        public PooledObject<StatefulRedisConnection> wrap(StatefulRedisConnection connection) {
            return new DefaultPooledObject<>(connection);
        }
    }

}
