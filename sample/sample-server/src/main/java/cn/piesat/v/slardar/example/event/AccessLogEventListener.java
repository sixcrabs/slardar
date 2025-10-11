package cn.piesat.v.slardar.example.event;

import org.winterfell.slardar.core.AuditLogIngest;
import org.winterfell.slardar.core.event.SlardarEventListener;
import org.winterfell.slardar.core.SlardarException;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 自定义日志 监听器
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/1/3
 */
@Component
public class AccessLogEventListener implements SlardarEventListener<AccessLogEvent> {

    private final AuditLogIngest auditLogIngest;

    public AccessLogEventListener(AuditLogIngest auditLogIngest) {
        this.auditLogIngest = auditLogIngest;
    }

    /**
     * handle event
     *
     * @param event
     * @throws SlardarException
     */
    @Override
    public void onEvent(AccessLogEvent event) throws SlardarException {
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
    public boolean support(Class<AccessLogEvent> eventClass) {
        return AccessLogEvent.class.equals(eventClass);
    }
}
