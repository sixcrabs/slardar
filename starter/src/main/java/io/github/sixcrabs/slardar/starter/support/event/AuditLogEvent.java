package io.github.sixcrabs.slardar.starter.support.event;

import io.github.sixcrabs.slardar.core.event.SlardarEvent;
import io.github.sixcrabs.slardar.core.domain.AuditLog;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/6/21
 */
public class AuditLogEvent implements SlardarEvent<AuditLog> {

    private final AuditLog data;

    public AuditLogEvent(AuditLog data) {
        this.data = data;
    }

    /**
     * payload with the event
     *
     * @return
     */
    @Override
    public AuditLog payload() {
        return data;
    }
}