package org.winterfell.slardar.starter.authenticate.handler;

import org.winterfell.slardar.core.SlardarException;
import org.winterfell.slardar.spi.SlardarSpiContext;
import cn.piesat.v.misc.hutool.mini.StringUtil;
import org.winterfell.slardar.starter.authenticate.handler.impl.DefaultSlardarAuthenticateHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * <p>
 * 工厂类 管理 认证 handler 实现
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/23
 */
public class SlardarAuthenticateHandlerFactory implements InitializingBean, DisposableBean {

    private final SlardarSpiContext slardarContext;

    private static final Map<String, SlardarAuthenticateHandler> REPO = new HashMap<>(1);

    private static final Logger logger = LoggerFactory.getLogger(SlardarAuthenticateHandlerFactory.class);

    public SlardarAuthenticateHandlerFactory(SlardarSpiContext slardarContext) {
        this.slardarContext = slardarContext;
    }

    public SlardarAuthenticateHandler findAuthenticateHandler(String type) throws SlardarException {
        if (StringUtil.isBlank(type)) {
            return REPO.get(DefaultSlardarAuthenticateHandlerImpl.NAME);
        }
        if (REPO.containsKey(type)) {
            return REPO.get(type);
        } else {
            throw new SlardarException("未找到[{}]认证对应实现类", type);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 获取所有的认证实现 并存入 缓存
        ServiceLoader<SlardarAuthenticateHandler> impls = ServiceLoader.load(SlardarAuthenticateHandler.class);
        for (SlardarAuthenticateHandler impl : impls) {
            impl.initialize(this.slardarContext);
            REPO.put(impl.name(), impl);
        }
        logger.info("[slardar] 已加载 [{}] 个认证组件实现", REPO.size());

    }

    /**
     * Invoked by the containing {@code BeanFactory} on destruction of a bean.
     *
     * @throws Exception in case of shutdown errors. Exceptions will get logged
     *                   but not rethrown to allow other beans to release their resources as well.
     */
    @Override
    public void destroy() throws Exception {
        REPO.values().forEach(SlardarAuthenticateHandler::destroy);
    }
}
