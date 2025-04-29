package cn.piesat.v.slardar.starter.support.store;

import cn.hutool.core.util.StrUtil;
import cn.piesat.v.misc.hutool.mini.StringUtil;
import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.spi.SlardarKeyStore;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.starter.config.SlardarProperties;
import com.google.auto.service.AutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
        SlardarProperties properties = context.getBeanIfAvailable(SlardarProperties.class);
        String type = properties.getKeyStore().getType();
        if (type.equalsIgnoreCase("mapdb")) {
            logger.info("[keystore] 初始化 mapdb ....");
            Path path = StrUtil.isBlank(properties.getKeyStore().getUri()) ?
                    Paths.get(System.getProperty("user.home"), "keystore.db") : Paths.get(properties.getKeyStore().getUri());
            File dbFile = path.toFile();
            logger.info("[keystore] use data file: {}", dbFile.getPath());
        }

    }
}
