package cn.piesat.nj.slardar.starter.authenticate.mfa;

import cn.hutool.core.util.StrUtil;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.authenticate.crypto.SlardarCrypto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import static cn.piesat.nj.slardar.starter.authenticate.crypto.impl.AESCrypto.MODE;

/**
 * <p>
 * factory for otp dispatcher
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/29
 */
public class OtpDispatcherFactory implements InitializingBean {

    private final SlardarContext slardarContext;

    private static final Map<String, OtpDispatcher> REPO = new HashMap<>(1);

    public static final Logger logger = LoggerFactory.getLogger(OtpDispatcherFactory.class);

    public OtpDispatcherFactory(SlardarContext slardarContext) {
        this.slardarContext = slardarContext;
    }

    public OtpDispatcher findDispatcher(String mode) throws SlardarException {
        if (StrUtil.isBlank(mode)) {
            return REPO.get(MODE);
        }
        if (REPO.containsKey(mode.toUpperCase())) {
            return REPO.get(mode.toUpperCase());
        } else {
            throw new SlardarException("未找到[{}]对应实现类", mode);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 获取所有的 otp 发送实现 并存入 缓存
        ServiceLoader<OtpDispatcher> impls = ServiceLoader.load(OtpDispatcher.class);
        for (OtpDispatcher impl : impls) {
            impl.setContext(this.slardarContext);
            REPO.put(impl.mode().toUpperCase(), impl);
        }
        logger.info("[slardar] 已加载 [{}] 个OTP发送组件实现", REPO.size());
    }
}
