package io.github.sixcrabs.slardar.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.sixcrabs.slardar.core.SlardarContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import io.github.sixcrabs.slardar.starter.support.store.SlardarRedisKeyStoreImpl;

import java.util.Collection;
import java.util.Map;

/**
 * <p>
 * spring impl of spi context
 * </p>
 *
 * @author alex
 * @version v1.0 2023/11/20
 */
public class SpringSlardarContextImpl implements SlardarContext, ApplicationContextAware {

    private ApplicationContext context;

    private final SlardarProperties properties;

    private static final Logger logger = LoggerFactory.getLogger(SpringSlardarContextImpl.class);

    public SpringSlardarContextImpl(SlardarProperties properties) {
        this.properties = properties;
    }

    /**
     * initialize
     */
    @Override
    public void initialize() {
        boolean cluster = properties.isCluster();
        if (cluster) {
            // 检测 keystore 的配置必须是 redis
            boolean isRedis = SlardarRedisKeyStoreImpl.NAME.equalsIgnoreCase(properties.getKeyStore().getType());
            if (!isRedis) {
                throw new RuntimeException("[slardar] must use `redis` as keystore when cluster mode is on");
            }
            logger.info("[slardar] 集群模式已开启");
        }
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    /**
     * get beans by type
     *
     * @param clazz
     * @return
     */
    @Override
    public <T> Collection<T> getBeans(Class<T> clazz) {
        Map<String, T> beansOfType = context.getBeansOfType(clazz);
        return beansOfType.values();
    }

    @Override
    public <T> T getBeanIfAvailable(Class<T> clazz) {
        ObjectProvider<T> provider = getBeanProvider(clazz);
        return provider.getIfAvailable();
    }

    public <T> ObjectProvider<T> getBeanProvider(Class<T> clazz) {
        return context.getBeanProvider(clazz);
    }

    @Override
    public <T> T getBeanOrDefault(Class<T> clazz, T defaultValue) {
        try {
            ObjectProvider<T> provider = getBeanProvider(clazz);
            return provider.getIfAvailable(() -> defaultValue);
        } catch (BeansException e) {
            return defaultValue;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        context = ctx;
    }
}