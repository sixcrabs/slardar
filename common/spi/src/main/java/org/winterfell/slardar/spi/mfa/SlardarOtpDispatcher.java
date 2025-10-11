package org.winterfell.slardar.spi.mfa;

import org.winterfell.slardar.core.SlardarException;
import org.winterfell.slardar.core.entity.Account;
import org.winterfell.slardar.spi.SlardarSpi;

/**
 * <p>
 * otp 发送 SPI
 * </p>
 *
 * @author alex
 * @version v1.0 2023/11/20
 */
public interface SlardarOtpDispatcher extends SlardarSpi {

    /**
     *  发布 code
     * @param otpCode  otp 验证码
     * @param account  账户信息
     * @return
     * @throws SlardarException
     */
    OtpDispatchResult dispatch(String otpCode, Account account) throws SlardarException;
}
