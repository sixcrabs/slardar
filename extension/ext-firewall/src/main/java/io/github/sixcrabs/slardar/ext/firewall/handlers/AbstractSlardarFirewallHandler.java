package io.github.sixcrabs.slardar.ext.firewall.handlers;

import lombok.Data;
import io.github.sixcrabs.slardar.ext.firewall.SlardarFirewallHandler;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/10/22
 */
public abstract class AbstractSlardarFirewallHandler<T extends AbstractSlardarFirewallHandler.Setting> implements SlardarFirewallHandler {

    protected final T setting;

    protected AbstractSlardarFirewallHandler(T setting) {
        this.setting = setting;
    }

    @Data
    public abstract static class Setting {
        private boolean enabled = false;
    }
}