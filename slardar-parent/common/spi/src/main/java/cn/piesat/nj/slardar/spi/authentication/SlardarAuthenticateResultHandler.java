package cn.piesat.nj.slardar.spi.authentication;

import cn.piesat.nj.slardar.core.AccountInfoDTO;
import cn.piesat.nj.slardar.core.entity.Account;
import cn.piesat.nj.slardar.spi.SlardarSpi;

import java.util.Map;

/**
 * <p>
 * 自定义扩展认证成功和失败的返回信息
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/12/21
 */
public interface SlardarAuthenticateResultHandler extends SlardarSpi {


    /**
     * 认证成功结果
     * @param accountInfoDTO
     * @return
     */
    Map<String, Object> authSucceedResult(AccountInfoDTO accountInfoDTO);

    /**
     * 认证失败结果
     * @param exception
     * @return
     */
    Map<String, Object> authFailedResult(RuntimeException exception);

    /**
     * 无权限访问结果
     * @param exception
     * @return
     */
    Map<String, Object> authDeniedResult(RuntimeException exception);
}
