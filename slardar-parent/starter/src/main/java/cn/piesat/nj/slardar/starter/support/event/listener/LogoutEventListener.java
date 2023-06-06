package cn.piesat.nj.slardar.starter.support.event.listener;

import cn.piesat.nj.slardar.core.AuditLogIngest;
import cn.piesat.nj.slardar.core.SlardarEventListener;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.core.entity.AuditLog;
import cn.piesat.nj.slardar.core.gateway.AuditLogGateway;
import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.support.event.LogoutEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * <p>
 * 退出事件处理
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/4/3
 */
@Component
public class LogoutEventListener implements SlardarEventListener<LogoutEvent> {

    private final AuditLogIngest auditLogIngest;

    public LogoutEventListener(SlardarContext slardarContext) {
        this.auditLogIngest = slardarContext.getAuditLogIngest();
    }

    @Override
    public void onEvent(LogoutEvent event) throws SlardarException {
        //
        try {
            LogoutEvent.Payload payload = event.payload();
            AuditLog auditLog = new AuditLog();
            auditLog.setAccountName(payload.getAccountName())
                    .setLogType("logout")
                    .setLogTime(LocalDateTime.now());
            auditLogIngest.ingest(auditLog);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean support(Class<LogoutEvent> eventClass) {
        return LogoutEvent.class.equals(eventClass);
    }

    @Override
    public int ordinal() {
        return SlardarEventListener.super.ordinal();
    }
}
