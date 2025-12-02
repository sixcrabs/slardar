package org.winterfell.slardar.ext.firewall.core;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.winterfell.slardar.core.SlardarContext;
import org.winterfell.slardar.core.SlardarException;


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