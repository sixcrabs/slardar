package cn.piesat.nj.slardar.starter.authenticate.mfa;

import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.core.entity.Account;
import cn.piesat.nj.slardar.starter.SlardarContext;

/**
 * <p>
 * OTP code 发送接口
 * 应用方可以实现该接口以定制为 其他发送方式
 * - 默认实现： email 方式
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/29
 */
public interface OtpDispatcher {

    /**
     * 发送模式
     * - email
     * - sms
     * - ...
     * @return
     */
    String mode();


    /**
     * set context
     * @param context
     */
    void setContext(SlardarContext context);


    /**
     *  发布 code
     * @param otpCode
     * @param account
     * @return
     * @throws SlardarException
     */
    OtpDispatchResult dispatch(String otpCode, Account account) throws SlardarException;


}
