package cn.piesat.nj.slardar.starter.support.event;

import cn.hutool.core.thread.ThreadUtil;
import cn.piesat.nj.slardar.core.SlardarEventListener;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.core.entity.AuditLog;
import cn.piesat.nj.slardar.core.gateway.AuditLogGateway;
import org.springframework.stereotype.Component;

/**
 * <p>
 * .
 * </p>
 *
 * @author alex
 * @version v1.0 2023/4/2
 */
@Component
public class LoginEventListener implements SlardarEventListener<LoginEvent> {

    // 写入审计日志
    private final AuditLogGateway auditLogGateway;

    public LoginEventListener(AuditLogGateway auditLogGateway) {
        this.auditLogGateway = auditLogGateway;
    }

    /**
     * handle event
     *
     * @param event
     * @throws SlardarException
     */
    @Override
    public void onEvent(LoginEvent event) throws SlardarException {
        // 记录用户的登录日志
        try {
            LoginEvent.LoginEventPayload payload = event.payload();
            auditLogGateway.create(new AuditLog()
                    .setAccountId(payload.getAccount().getId())
                    .setClientIp(payload.getRequest().getRemoteAddr())
                    .setAccountName(payload.getAccount().getName()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * support event or not
     *
     * @param eventClass
     * @return
     */
    @Override
    public boolean support(Class<LoginEvent> eventClass) {
        return eventClass.equals(LoginEvent.class);
    }
}
