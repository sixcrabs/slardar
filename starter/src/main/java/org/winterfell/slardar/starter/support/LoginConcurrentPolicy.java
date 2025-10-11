package org.winterfell.slardar.starter.support;

/**
 * <p>
 * 针对同端登录策略
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/27
 */
public enum LoginConcurrentPolicy {

    /**
     * 同端互斥 即相同账户只能在一个同端页面中有效登录
     */
    mutex,

    /**
     * 独立，不互斥，同一客户端 同个账户 每次都生成一个新的 token
     */
    separate


}
