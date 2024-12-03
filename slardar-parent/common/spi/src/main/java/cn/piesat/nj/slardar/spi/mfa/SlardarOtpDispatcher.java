package cn.piesat.nj.slardar.spi.mfa;

import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.core.entity.Account;
import cn.piesat.nj.slardar.spi.SlardarSpi;

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
