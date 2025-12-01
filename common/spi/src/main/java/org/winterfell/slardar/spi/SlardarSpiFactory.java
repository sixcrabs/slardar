package org.winterfell.slardar.spi;

import org.winterfell.slardar.core.SlardarException;
import org.winterfell.slardar.spi.authenticate.SlardarAuthenticateResultAdapter;
import org.winterfell.slardar.spi.captcha.SlardarCaptchaGenerator;
import org.winterfell.slardar.spi.crypto.SlardarCrypto;
import org.winterfell.slardar.spi.mfa.SlardarOtpDispatcher;
import org.winterfell.slardar.spi.token.SlardarTokenProvider;

/**
 * <p>
 * SPI factory
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/9/22
 */
public interface SlardarSpiFactory {

    /**
     * 寻找正确的加密实现
     * @param name
     * @return
     * @throws SlardarException
     */
    SlardarCrypto findCrypto(String name);

    /**
     * 寻找正确的 otp 发送器实现
     * @param name
     * @return
     * @throws SlardarException
     */
    SlardarOtpDispatcher findOtpDispatcher(String name);

    /**
     * 寻找正确的 token provider 实现
     * @param name
     * @return
     * @throws SlardarException
     */
    SlardarTokenProvider findTokenProvider(String name);

    /**
     * 寻找正确的认证结果适配器实现
     * @param name
     * @return
     * @throws SlardarException
     */
    SlardarAuthenticateResultAdapter findAuthenticateResultHandler(String name);

    /**
     * TODO: 备用
     * @param name
     * @return
     * @throws SlardarException
     */
    SlardarCaptchaGenerator findCaptchaGenerator(String name);

}