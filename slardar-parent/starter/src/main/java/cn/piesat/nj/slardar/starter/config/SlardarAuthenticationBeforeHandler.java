package cn.piesat.nj.slardar.starter.config;

import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.support.SlardarAuthenticationToken;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/4/14
 */
@FunctionalInterface
public interface SlardarAuthenticationBeforeHandler {

    /**
     * 在进入认证前 由应用前置处理，
     * 如
     * - 判断登录端类型
     * - 判断客户端ip等
     *
     * @throws SlardarException 抛出异常 则终止认证
     */
    void before(SlardarAuthenticationToken authenticationToken) throws SlardarException;
}
