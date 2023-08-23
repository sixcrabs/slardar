package cn.piesat.nj.slardar.starter.authenticate.handler;

import cn.hutool.core.util.StrUtil;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.token.SlardarToken;
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
public class SlardarAuthenticateHandlerFactory implements InitializingBean {

    private final SlardarContext slardarContext;

    private static final Map<String, SlardarAuthenticateHandler> REPO = new HashMap<>(1);

    public SlardarAuthenticateHandlerFactory(SlardarContext slardarContext) {
        this.slardarContext = slardarContext;
    }

    public SlardarAuthenticateHandler findAuthenticateHandler(String type) throws SlardarException {
        if (StrUtil.isBlank(type)) {
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
        // 获取所有的 token 实现 并存入 缓存
        ServiceLoader<SlardarAuthenticateHandler> impls = ServiceLoader.load(SlardarAuthenticateHandler.class);
        for (SlardarAuthenticateHandler impl : impls) {
            impl.setSlardarContext(this.slardarContext);
            REPO.put(impl.type(), impl);
        }
    }
}
