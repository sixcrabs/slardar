package cn.piesat.v.slardar.spi;

import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.spi.authenticate.SlardarAuthenticateResultAdapter;
import cn.piesat.v.slardar.spi.captcha.SlardarCaptchaGenerator;
import cn.piesat.v.slardar.spi.crypto.SlardarCrypto;
import cn.piesat.v.slardar.spi.mfa.SlardarOtpDispatcher;
import cn.piesat.v.slardar.spi.token.SlardarTokenProvider;

/**
 * <p>
 * SPI factory
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/9/22
 */
public interface SlardarSpiFactory {

    SlardarCrypto findCrypto(String name) throws SlardarException;

    SlardarOtpDispatcher findOtpDispatcher(String name) throws SlardarException;

    SlardarTokenProvider findTokenProvider(String name) throws SlardarException;

    SlardarCaptchaGenerator findCaptchaGenerator(String name) throws SlardarException;

    SlardarAuthenticateResultAdapter findAuthenticateResultHandler(String name) throws SlardarException;

}
