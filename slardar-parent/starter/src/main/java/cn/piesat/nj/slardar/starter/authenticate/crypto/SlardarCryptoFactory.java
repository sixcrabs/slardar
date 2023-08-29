package cn.piesat.nj.slardar.starter.authenticate.crypto;

import cn.hutool.core.util.StrUtil;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.authenticate.handler.DefaultSlardarAuthenticateHandlerImpl;
import cn.piesat.nj.slardar.starter.authenticate.handler.SlardarAuthenticateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import static cn.piesat.nj.slardar.starter.authenticate.crypto.impl.AESCrypto.MODE;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/28
 */
public class SlardarCryptoFactory implements InitializingBean {

    private final SlardarContext slardarContext;

    private static final Map<String, SlardarCrypto> REPO = new HashMap<>(1);

    public static final Logger logger = LoggerFactory.getLogger(SlardarCryptoFactory.class);

    public SlardarCryptoFactory(SlardarContext slardarContext) {
        this.slardarContext = slardarContext;
    }

    public SlardarCrypto findCrypto(String mode) throws SlardarException {
        if (StrUtil.isBlank(mode)) {
            return REPO.get(MODE);
        }
        if (REPO.containsKey(mode.toUpperCase())) {
            return REPO.get(mode.toUpperCase());
        } else {
            throw new SlardarException("未找到[{}]加密对应实现类", mode);
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        // 获取所有的 加解密 实现 并存入 缓存
        ServiceLoader<SlardarCrypto> impls = ServiceLoader.load(SlardarCrypto.class);
        for (SlardarCrypto impl : impls) {
            impl.setContext(this.slardarContext);
            REPO.put(impl.mode().toUpperCase(), impl);
        }
        logger.info("[slardar] 已加载 [{}] 个登录加密组件实现", REPO.size());
    }
}
