package cn.piesat.v.slardar.spi;

import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.spi.authenticate.SlardarAuthenticateResultAdapter;
import cn.piesat.v.slardar.spi.captcha.SlardarCaptchaGenerator;
import cn.piesat.v.slardar.spi.crypto.SlardarCrypto;
import cn.piesat.v.slardar.spi.mfa.SlardarOtpDispatcher;
import cn.piesat.v.slardar.spi.token.SlardarTokenProvider;

import javax.swing.*;

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
    SlardarCrypto findCrypto(String name) throws SlardarException;

    /**
     * 寻找正确的 otp 发送器实现
     * @param name
     * @return
     * @throws SlardarException
     */
    SlardarOtpDispatcher findOtpDispatcher(String name) throws SlardarException;

    /**
     * 寻找正确的 token provider 实现
     * @param name
     * @return
     * @throws SlardarException
     */
    SlardarTokenProvider findTokenProvider(String name) throws SlardarException;

    /**
     * 寻找正确的认证结果适配器实现
     * @param name
     * @return
     * @throws SlardarException
     */
    SlardarAuthenticateResultAdapter findAuthenticateResultHandler(String name) throws SlardarException;
    /**
     * 根据配置的名称找到对应的 keystore 实现
     * @param name
     * @return
     * @throws SlardarException
     */
    SlardarKeyStore findKeyStore(String name) throws SlardarException;

    /**
     * TODO: 备用
     * @param name
     * @return
     * @throws SlardarException
     */
    SlardarCaptchaGenerator findCaptchaGenerator(String name) throws SlardarException;

}
