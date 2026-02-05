package io.github.sixcrabs.slardar.ext.firewall.handlers;

import io.github.sixcrabs.slardar.core.SlardarContext;
import io.github.sixcrabs.slardar.core.SlardarException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

import static io.github.sixcrabs.slardar.ext.firewall.SlardarFirewallUtil.vagueMatch;

/**
 * <p>
 * host检测
 * </p>
 *
 * @author Alex
 * @since 2025/10/22
 */
public class SlardarFirewallHostsHandler extends AbstractSlardarFirewallHandler<SlardarFirewallHostsHandler.Setting> {

    public SlardarFirewallHostsHandler(SlardarFirewallHostsHandler.Setting setting) {
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
        String host = request.getRemoteHost();
        if (!hasElement(Arrays.asList(this.setting.getAllowedHosts()), host)) {
            throw new SlardarException("[slardar-firewall] 非法请求 host：" + host);
        }
    }

    private boolean hasElement(List<String> list, String element) {
        // 空集合直接返回false
        if (list == null || list.isEmpty()) {
            return false;
        }
        // 先尝试一下简单匹配，如果可以匹配成功则无需继续模糊匹配
        if (list.contains(element)) {
            return true;
        }
        // 开始模糊匹配
        for (String pattern : list) {
            if (vagueMatch(pattern, element)) {
                return true;
            }
        }

        // 走出for循环说明没有一个元素可以匹配成功
        return false;
    }

    ;

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ConfigurationProperties(prefix = "slardar.firewall.hosts")
    public static class Setting extends AbstractSlardarFirewallHandler.Setting {

        private String[] allowedHosts = new String[0];
    }
}