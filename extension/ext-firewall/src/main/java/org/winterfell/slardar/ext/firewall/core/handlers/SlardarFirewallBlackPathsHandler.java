package org.winterfell.slardar.ext.firewall.core.handlers;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.winterfell.slardar.core.SlardarContext;
import org.winterfell.slardar.core.SlardarException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 路径黑名单限制
 * </p>
 *
 * @author Alex
 * @since 2025/10/22
 */
public class SlardarFirewallBlackPathsHandler extends AbstractSlardarFirewallHandler<SlardarFirewallBlackPathsHandler.Setting> {


    public SlardarFirewallBlackPathsHandler(SlardarFirewallBlackPathsHandler.Setting setting) {
        super(setting);
    }

    /**
     * 执行校验
     *
     * @param request  请求对象
     * @param response 响应对象
     * @param context  上下文 用于获取bean等
     * @param params   预留扩展参数
     */
    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, SlardarContext context, Object params) throws SlardarException {
        String requestPath = request.getRequestURI();
        for (String item : this.setting.getBlackPaths()) {
            if (requestPath.equals(item)) {
                throw new SlardarException("[slardar-firewall] invalid request：" + requestPath, requestPath);
            }
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ConfigurationProperties(prefix = "slardar.firewall.black-paths")
    public static class Setting extends AbstractSlardarFirewallHandler.Setting {

        private String[] blackPaths = new String[0];
    }
}
