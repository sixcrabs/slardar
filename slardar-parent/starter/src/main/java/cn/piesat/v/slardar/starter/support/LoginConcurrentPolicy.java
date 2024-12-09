package cn.piesat.v.slardar.starter.support;

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
     * 同端互斥 即相同账户只能在一个 同端页面中有效登录
     */
    mutex,
    /**
     * 不互斥，共享同一个 已有token
     */
    share,

    /**
     * 不互斥，每次都生成一个 token
     */
    separate


}
