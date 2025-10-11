package org.winterfell.slardar.spi.authenticate;

import org.winterfell.slardar.core.AccountInfoDTO;
import org.winterfell.slardar.spi.SlardarSpi;

import java.util.Map;

/**
 * <p>
 * 自定义扩展认证成功和失败的返回信息
 * 应用方可以实现此接口 用于定制认证成功/失败/拒绝等的信息
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/12/21
 */
public interface SlardarAuthenticateResultAdapter extends SlardarSpi {


    /**
     * 认证成功结果
     * @param accountInfoDTO  账户信息
     * @return
     */
    Map<String, Object> authSucceedResult(AccountInfoDTO accountInfoDTO);

    /**
     * 认证失败结果
     * @param exception  认证异常
     * @return
     */
    Map<String, Object> authFailedResult(RuntimeException exception);

    /**
     * 无权限访问结果
     * @param exception 认证异常
     * @return
     */
    Map<String, Object> authDeniedResult(RuntimeException exception);
}
