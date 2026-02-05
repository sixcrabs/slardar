package io.github.sixcrabs.slardar.ext.firewall.handlers;

import io.github.sixcrabs.slardar.core.SlardarContext;
import io.github.sixcrabs.slardar.core.SlardarException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 非法请求头
 * </p>
 *
 * @author Alex
 * @since 2025/10/22
 */
public class SlardarFirewallHeadersHandler extends AbstractSlardarFirewallHandler<SlardarFirewallHeadersHandler.Setting> {

    public SlardarFirewallHeadersHandler(SlardarFirewallHeadersHandler.Setting setting) {
        super(setting);
    }

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
        for (String headerName : this.setting.getForbiddenNames()) {
            if (request.getHeader(headerName) != null) {
                throw new SlardarException("[slardar-firewall] 非法请求头：" + headerName);
            }
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ConfigurationProperties(prefix = "slardar.firewall.headers")
    public static class Setting extends AbstractSlardarFirewallHandler.Setting {

        private String[] forbiddenNames = new String[0];
    }
}