package cn.piesat.nj.slardar.starter;

import cn.hutool.core.util.StrUtil;
import cn.piesat.nj.misc.hutool.mini.StringUtil;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.spi.SlardarSpiContext;
import cn.piesat.nj.slardar.spi.SlardarSpiFactory;
import cn.piesat.nj.slardar.spi.authentication.SlardarAuthenticateResultHandler;
import cn.piesat.nj.slardar.spi.captcha.SlardarCaptchaGenerator;
import cn.piesat.nj.slardar.spi.crypto.SlardarCrypto;
import cn.piesat.nj.slardar.spi.mfa.SlardarOtpDispatcher;
import cn.piesat.nj.slardar.spi.token.SlardarTokenProvider;
import cn.piesat.nj.slardar.starter.handler.SlardarDefaultAuthenticateResultHandler;
import cn.piesat.nj.slardar.starter.support.spi.EmailOtpDispatcher;
import cn.piesat.nj.slardar.starter.support.spi.token.SlardarTokenProviderJwtImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import static cn.piesat.nj.slardar.starter.support.spi.crypto.AESCrypto.MODE;

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

    private final SlardarSpiContext spiContext;

    /**
     * crypto repo
     */
    private static final Map<String, SlardarCrypto> CRYPTO_REPO = new HashMap<>(1);
    private static final Map<String, SlardarOtpDispatcher> OTP_REPO = new HashMap<>(1);

    private static final Map<String, SlardarTokenProvider> TOKEN_REPO = new HashMap<>(1);

    private static final Map<String, SlardarAuthenticateResultHandler> RESULT_HANDLER_REPO = new HashMap<>(1);

    public SpringSlardarSpiFactory(SlardarSpiContext spiContext) {
        this.spiContext = spiContext;
    }

    @Override
    public SlardarCrypto findCrypto(String name) throws SlardarException {
        if (StrUtil.isBlank(name)) {
            return CRYPTO_REPO.get(MODE);
        }
        if (CRYPTO_REPO.containsKey(name.toUpperCase())) {
            return CRYPTO_REPO.get(name.toUpperCase());
        } else {
            throw new SlardarException("未找到[{}]加密对应实现类", name);
        }
    }

    @Override
    public SlardarOtpDispatcher findOtpDispatcher(String name) throws SlardarException {
        if (StringUtil.isBlank(name)) {
            return OTP_REPO.get(EmailOtpDispatcher.MODE);
        }
        if (OTP_REPO.containsKey(name.toUpperCase())) {
            return OTP_REPO.get(name.toUpperCase());
        } else {
            throw new SlardarException("未找到[{}]对应实现类", name);
        }
    }

    @Override
    public SlardarTokenProvider findTokenProvider(String name) throws SlardarException {
        if (StringUtil.isBlank(name)) {
            return new SlardarTokenProviderJwtImpl();
        }
        if (TOKEN_REPO.containsKey(name.toUpperCase())) {
            return TOKEN_REPO.get(name.toUpperCase());
        } else {
            throw new SlardarException("未找到[{}]对应实现类", name);
        }
    }

    /**
     * TODO:
     *
     * @param name
     * @return
     * @throws SlardarException
     */
    @Override
    public SlardarCaptchaGenerator findCaptchaGenerator(String name) throws SlardarException {
        return null;
    }

    @Override
    public SlardarAuthenticateResultHandler findAuthenticateResultHandler(String name) throws SlardarException {
        if (StringUtil.isBlank(name)) {
            return RESULT_HANDLER_REPO.get(SlardarDefaultAuthenticateResultHandler.NAME);
        }
        if (RESULT_HANDLER_REPO.containsKey(name.toUpperCase())) {
            return RESULT_HANDLER_REPO.get(name.toUpperCase());
        } else {
            throw new SlardarException("未找到[{}]对应实现类", name);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 加载所有需要的 SPI 实现
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

        ServiceLoader<SlardarAuthenticateResultHandler> authenticateResultHandlers = ServiceLoader.load(SlardarAuthenticateResultHandler.class);
        for (SlardarAuthenticateResultHandler resultHandler : authenticateResultHandlers) {
            resultHandler.initialize(this.spiContext);
            RESULT_HANDLER_REPO.put(resultHandler.name().toUpperCase(), resultHandler);
        }
        logger.info("[slardar] 已加载 [{}] 个认证结果处理组件", RESULT_HANDLER_REPO.size());


    }
}
