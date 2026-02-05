package org.winterfell.slardar.example.ext;

import org.springframework.stereotype.Component;
import io.github.sixcrabs.slardar.core.SlardarContext;
import io.github.sixcrabs.slardar.core.SlardarException;
import io.github.sixcrabs.slardar.ext.firewall.SlardarFirewallHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * <p>
 * 自定义接口防火墙功能
 * </p>
 *
 * @author Alex
 * @since 2025/10/22
 */
@Component
public class MyFirewallHandler implements SlardarFirewallHandler {

    /**
     * 执行校验
     *
     * @param request  请求对象
     * @param response 响应对象
     * @param context  上下文 用于在运行时获取bean等
     * @param params   预留扩展参数
     */
    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, SlardarContext context, Object params) throws SlardarException {
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            if (parameterName.contains("xss")) {
                throw new SlardarException("参数中包含敏感词!");
            }
        }
    }

    /**
     * 是否启用该 handler 默认true
     *
     * @return
     */
    @Override
    public boolean isEnabled() {
        // false : 不启用
        return false;
    }
}