package cn.piesat.nj.slardar.example.event;

import cn.piesat.nj.slardar.core.SlardarEvent;
import cn.piesat.nj.slardar.core.entity.AuditLog;

import java.io.Serializable;

/**
 * <p>
 * 演示如何自定义日志
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/1/3
 */
public class AccessLogEvent implements SlardarEvent<AuditLog> {

    private final AuditLog data;

    public AccessLogEvent(AuditLog data) {
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
