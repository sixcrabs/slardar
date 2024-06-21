package cn.piesat.nj.slardar.starter.support.event.listener;

import cn.piesat.nj.slardar.core.AuditLogIngest;
import cn.piesat.nj.slardar.core.SlardarEventListener;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.support.event.AuditLogEvent;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 处理 auditLog 事件
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/6/21
 */
@Component
public class AuditLogEventListener implements SlardarEventListener<AuditLogEvent> {

    private final AuditLogIngest auditLogIngest;
    public AuditLogEventListener(AuditLogIngest auditLogIngest) {
        this.auditLogIngest = auditLogIngest;
    }

    /**
     * handle event
     *
     * @param event
     * @throws SlardarException
     */
    @Override
    public void onEvent(AuditLogEvent event) throws SlardarException {
        // 这里调用 audit log
        if (event != null && event.payload() != null) {
            auditLogIngest.ingest(event.payload());
        }
    }

    /**
     * support event or not
     *
     * @param eventClass
     * @return
     */
    @Override
    public boolean support(Class<AuditLogEvent> eventClass) {
        return AuditLogEvent.class.equals(eventClass);
    }
}
