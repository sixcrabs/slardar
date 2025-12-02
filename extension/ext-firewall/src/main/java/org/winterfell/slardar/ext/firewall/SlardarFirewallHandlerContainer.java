package org.winterfell.slardar.ext.firewall;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.winterfell.slardar.core.SlardarContext;
import org.winterfell.slardar.core.SlardarException;
import org.winterfell.slardar.ext.firewall.core.SlardarFirewallHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * firewallHandler 管理容器
 * - 添加handler
 * - 设置handler执行顺序
 * - 停用handler等
 * </p>
 *
 * @author Alex
 * @since 2025/10/22
 */
public class SlardarFirewallHandlerContainer {

    private static final Logger logger = LoggerFactory.getLogger(SlardarFirewallHandlerContainer.class);

    private static class SlardarFirewallHandlerContainerHolder {
        private static final SlardarFirewallHandlerContainer INSTANCE = new SlardarFirewallHandlerContainer();
    }

    public static SlardarFirewallHandlerContainer getInstance() {
        return SlardarFirewallHandlerContainerHolder.INSTANCE;
    }

    private final List<SlardarFirewallHandler> handlers = new ArrayList<>();

    private SlardarFirewallHandlerContainer() {
    }

    /**
     * 添加一个执行器 指定索引
     *
     * @param handler
     * @param index
     */
    public void addHandler(SlardarFirewallHandler handler, int index) {
        handlers.add(index, handler);
        logger.info("[slardar-firewall] 添加防火墙执行器: {}", handler.getClass().getSimpleName());
    }

    /**
     * 添加一个执行器 到最后执行
     *
     * @param handler
     */
    public void addHandler(SlardarFirewallHandler handler) {
        handlers.add(handler);
        logger.info("[slardar-firewall] 添加防火墙执行器: {}", handler.getClass().getSimpleName());
    }

    /**
     * 添加一个执行器到第一位执行
     *
     * @param handler
     */
    public void addHandlerToFirst(SlardarFirewallHandler handler) {
        addHandler(handler, 0);
    }

    public void execute(HttpServletRequest request, HttpServletResponse response, SlardarContext context) throws SlardarException {
        for (SlardarFirewallHandler handler : handlers) {
            handler.execute(request, response, context, null);
        }
    }
}