package io.github.sixcrabs.slardar.ext.firewall;

import io.github.sixcrabs.slardar.core.SlardarContext;
import io.github.sixcrabs.slardar.core.SlardarException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 防火墙钩子函数 接口
 * </p>
 *
 * @author Alex
 * @since 2025/10/22
 */
@FunctionalInterface
public interface SlardarFirewallHandler {

    /**
     * 执行校验
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param context 上下文 用于在运行时获取bean等
     * @param params 预留扩展参数
     */
    void execute(HttpServletRequest request, HttpServletResponse response, SlardarContext context, Object params) throws SlardarException;

    /**
     * 是否启用该 handler 默认true
     * @return
     */
    default boolean isEnabled() {
        return true;
    }
}