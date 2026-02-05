package io.github.sixcrabs.slardar.starter;

import io.github.sixcrabs.slardar.core.SlardarException;
import io.github.sixcrabs.slardar.core.SlardarContext;
import io.github.sixcrabs.slardar.spi.SlardarSpiFactory;
import io.github.sixcrabs.slardar.spi.authenticate.SlardarAuthenticateResultAdapter;
import io.github.sixcrabs.slardar.spi.captcha.SlardarCaptchaGenerator;
import io.github.sixcrabs.slardar.spi.crypto.SlardarCrypto;
import io.github.sixcrabs.slardar.spi.mfa.SlardarOtpDispatcher;
import io.github.sixcrabs.slardar.spi.token.SlardarTokenProvider;
import io.github.sixcrabs.slardar.starter.handler.SlardarDefaultAuthenticateResultAdapter;
import io.github.sixcrabs.slardar.starter.support.spi.EmailOtpDispatcher;
import io.github.sixcrabs.slardar.starter.support.spi.crypto.AESCrypto;
import io.github.sixcrabs.slardar.starter.support.spi.token.SlardarTokenJwtProvider;
import io.github.sixcrabs.slardar.spi.SlardarKeyStore;
import io.github.sixcrabs.slardar.starter.support.store.SlardarMemoryKeyStoreImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * <p>
 * .
 * </p>
 *
 * @author alex
 * @version v1.0 2023/11/20
 */
public class SpringSlardarSpiFactory implements SlardarSpiFactory, InitializingBean {

    public static final Logger logger = LoggerFactory.getLogger(SpringSlardarSpiFactory.class);

    private final SlardarContext spiContext;

    /**
     * crypto repo
     */
    private static final Map<String, SlardarCrypto> CRYPTO_REPO = new HashMap<>(1);

    private static final Map<String, SlardarOtpDispatcher> OTP_REPO = new HashMap<>(1);

    private static final Map<String, SlardarTokenProvider> TOKEN_REPO = new HashMap<>(1);

    private static final Map<String, SlardarAuthenticateResultAdapter> RESULT_HANDLER_REPO = new HashMap<>(1);

    private static final Map<String, SlardarKeyStore> KEY_STORE_REPO = new HashMap<>(1);

    public SpringSlardarSpiFactory(SlardarContext spiContext) {
        this.spiContext = spiContext;
    }

    @Override
    public SlardarCrypto findCrypto(String name) {
        return CRYPTO_REPO.computeIfAbsent(name.toUpperCase(), k -> {
            logger.warn("未找到 [{}] 对应的Crypto实现类, 采用默认实现", name);
            return CRYPTO_REPO.get(AESCrypto.MODE);
        });
    }

    @Override
    public SlardarOtpDispatcher findOtpDispatcher(String name) {
        return OTP_REPO.computeIfAbsent(name.toUpperCase(), k -> {
            logger.warn("未找到 [{}] 对应的OtpDispatcher实现类, 采用默认的实现", name);
            return OTP_REPO.get(EmailOtpDispatcher.MODE);
        });
    }

    @Override
    public SlardarTokenProvider findTokenProvider(String name) {
        return TOKEN_REPO.computeIfAbsent(name.toUpperCase(), k -> {
            logger.warn("未找到 [{}] 对应的TokenProvider实现类, 采用默认的实现", name);
            return TOKEN_REPO.get(SlardarTokenJwtProvider.NAME);
        });
    }

    @Override
    public SlardarAuthenticateResultAdapter findAuthenticateResultHandler(String name) {
        return RESULT_HANDLER_REPO.computeIfAbsent(name.toUpperCase(), k -> {
            logger.warn("未找到 [{}] 对应的AuthenticateResultHandler实现类, 采用默认的实现", name);
            return RESULT_HANDLER_REPO.get(SlardarDefaultAuthenticateResultAdapter.NAME);
        });
    }

    /**
     * 根据配置寻找合适的 keystore 实现
     *
     * @param name
     * @return
     * @throws SlardarException
     */
    @Override
    public SlardarKeyStore findKeyStore(String name) {
        return KEY_STORE_REPO.computeIfAbsent(name.toUpperCase(), k -> {
            logger.warn("未找到 [{}] 对应的KeyStore实现类, 采用默认的实现", name);
            return KEY_STORE_REPO.get(SlardarMemoryKeyStoreImpl.NAME);
        });
    }

    /**
     * TODO:
     *
     * @param name
     * @return
     * @throws SlardarException
     */
    @Override
    public SlardarCaptchaGenerator findCaptchaGenerator(String name) {
        return null;
    }

    /**
     * 加载所有需要的 SPI 实现并存入缓存
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            // 获取所有的 otp 发送实现 并存入 缓存
            ServiceLoader<SlardarOtpDispatcher> impls = ServiceLoader.load(SlardarOtpDispatcher.class);
            for (SlardarOtpDispatcher impl : impls) {
                impl.initialize(this.spiContext);
                OTP_REPO.put(impl.name().toUpperCase(), impl);
            }
            logger.info("[slardar] 已加载 [{}] 个OTP发送组件实现", OTP_REPO.size());

            // 获取所有的 加解密 实现 并存入 缓存
            ServiceLoader<SlardarCrypto> cryptos = ServiceLoader.load(SlardarCrypto.class);
            for (SlardarCrypto impl : cryptos) {
                impl.initialize(this.spiContext);
                CRYPTO_REPO.put(impl.name().toUpperCase(), impl);
            }
            logger.info("[slardar] 已加载 [{}] 个登录加密组件实现", CRYPTO_REPO.size());

            // 获取所有的 token 实现 并存入 缓存
            ServiceLoader<SlardarTokenProvider> tokenProviders = ServiceLoader.load(SlardarTokenProvider.class);
            for (SlardarTokenProvider tokenImpl : tokenProviders) {
                tokenImpl.initialize(this.spiContext);
                TOKEN_REPO.put(tokenImpl.name().toUpperCase(), tokenImpl);
            }
            logger.info("[slardar] 已加载 [{}] 个token实现组件", TOKEN_REPO.size());

            ServiceLoader<SlardarAuthenticateResultAdapter> authenticateResultHandlers = ServiceLoader.load(SlardarAuthenticateResultAdapter.class);
            for (SlardarAuthenticateResultAdapter resultHandler : authenticateResultHandlers) {
                resultHandler.initialize(this.spiContext);
                RESULT_HANDLER_REPO.put(resultHandler.name().toUpperCase(), resultHandler);
            }
            logger.info("[slardar] 已加载 [{}] 个认证结果处理组件", RESULT_HANDLER_REPO.size());

            ServiceLoader<SlardarKeyStore> keyStores = ServiceLoader.load(SlardarKeyStore.class);
            for (SlardarKeyStore keyStore : keyStores) {
                keyStore.initialize(this.spiContext);
                KEY_STORE_REPO.put(keyStore.name().toUpperCase(), keyStore);
            }
            logger.info("[slardar] 已加载 [{}] 个 kv 存储组件", KEY_STORE_REPO.size());
        } catch (Exception e) {
            logger.error("[slardar] spi load error {}", e.getLocalizedMessage());
        }

    }
}