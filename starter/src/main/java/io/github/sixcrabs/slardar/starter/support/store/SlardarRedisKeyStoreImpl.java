package io.github.sixcrabs.slardar.starter.support.store;

import org.winterfell.misc.hutool.mini.StringUtil;
import io.github.sixcrabs.slardar.spi.SlardarKeyStore;
import io.github.sixcrabs.slardar.core.SlardarContext;
import io.github.sixcrabs.slardar.starter.SlardarProperties;
import com.google.auto.service.AutoService;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.api.sync.RedisKeyCommands;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.function.Function;

import static io.github.sixcrabs.slardar.starter.support.store.KeyStoreUtil.GSON;
import static io.github.sixcrabs.slardar.starter.support.store.KeyStoreUtil.stringify;

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

    public static final String TYPE_SUFFIX = "_type";

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
    public void initialize(SlardarContext context) {
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
    public boolean set(String key, @NotNull Object val) {
        return Boolean.TRUE.equals(executeWithConnection(conn -> {
            RedisCommands<String, String> commands = conn.sync();
            commands.set(key, stringify(val));
            commands.set(key.concat(TYPE_SUFFIX), val.getClass().getName());
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
            RedisStringCommands<String, String> commands = conn.sync();
            return commands.setnx(key, stringify(val)) && commands.setnx(key.concat(TYPE_SUFFIX), val.getClass().getName());
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
            RedisStringCommands<String, String> commands = conn.sync();
            String reply = commands.setex(key, ttl, stringify(val));
            String replyTyped = commands.setex(key.concat(TYPE_SUFFIX), ttl, val.getClass().getName());
            return "OK".equalsIgnoreCase(reply) && "OK".equalsIgnoreCase(replyTyped);
        }));
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
        return executeWithConnection(conn -> {
            RedisStringCommands<String, String> stringCommands = conn.sync();
            RedisKeyCommands<String, Object> keyCommands = conn.sync();
            if (keyCommands.exists(key) > 0) {
                String valStr = stringCommands.get(key);
                // get type
                String clazz = stringCommands.get(key.concat(TYPE_SUFFIX));
                try {
                    Class<?> aClass = Class.forName(clazz);
                    if (KeyStoreUtil.isPrimitive(aClass)) {
                        return (T) KeyStoreUtil.stringToPrimitive(valStr, aClass);
                    } else {
                        return (T) GSON.fromJson(valStr, aClass);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        });
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
            RedisKeyCommands<String, Object> commands = conn.sync();
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
            RedisKeyCommands<String, Object> commands = conn.sync();
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
        boolean succeed = Boolean.TRUE.equals(executeWithConnection(conn -> {
            RedisKeyCommands<String, String> commands = conn.sync();
            return commands.del(key, key.concat(TYPE_SUFFIX)) > 0L;
        }));
        if (succeed) {
            notifyListeners(REMOVED, key, null);
        }
    }

    /**
     * TESTME:返回类似的 key 集合
     *
     * @param prefix 前缀 eg: `user_` 即返回所有以此开头的 key
     * @return
     */
    @Override
    public List<String> keys(String prefix) {
        return executeWithConnection(conn -> {
            RedisKeyCommands<String, Object> commands = conn.sync();
            return commands.keys(prefix.concat(".*"));
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