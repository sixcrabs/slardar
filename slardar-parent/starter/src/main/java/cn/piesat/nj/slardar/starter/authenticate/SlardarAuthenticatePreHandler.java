package cn.piesat.nj.slardar.starter.authenticate;

import cn.piesat.nj.slardar.core.SlardarException;

/**
 * <p>
 * 身份认证前置处理，应用可实现该接口用于处理特殊逻辑
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/4/14
 */
@FunctionalInterface
public interface SlardarAuthenticatePreHandler {

    /**
     * 在进入认证前 由应用前置处理，
     * 如
     * - 判断登录端类型
     * - 判断客户端ip等
     * @param authentication 认证数据对象
     * @throws SlardarException 抛出异常 则终止认证
     */
    void preHandle(SlardarAuthentication authentication) throws SlardarException;
}
