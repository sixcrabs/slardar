package io.github.sixcrabs.slardar.starter.support.event.listener;

import io.github.sixcrabs.slardar.starter.provider.AuditLogIngest;
import io.github.sixcrabs.slardar.core.event.SlardarEventListener;
import io.github.sixcrabs.slardar.core.SlardarException;
import io.github.sixcrabs.slardar.core.domain.AuditLog;
import io.github.sixcrabs.slardar.core.SlardarContext;
import io.github.sixcrabs.slardar.starter.support.HttpServletUtil;
import io.github.sixcrabs.slardar.starter.support.LoginDeviceType;
import io.github.sixcrabs.slardar.starter.support.event.LogoutEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Objects;

import static io.github.sixcrabs.slardar.starter.support.HttpServletUtil.getDeviceType;

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
        this.auditLogIngest = slardarContext.getBeanIfAvailable(AuditLogIngest.class);
    }

    @Override
    public void onEvent(LogoutEvent event) throws SlardarException {
        //
        try {
            LogoutEvent.Payload payload = event.payload();
            AuditLog auditLog = new AuditLog();
            HttpServletRequest request = payload.getRequest();
            LoginDeviceType deviceType = getDeviceType(request);
            String requestIpAddress = HttpServletUtil.getRequestIpAddress(request);
            auditLog.setAccountName(payload.getAccount().getName())
                    .setLogType("logout")
                    .setLogTime(LocalDateTime.now())
                    .setClientType(Objects.isNull(deviceType) ? "unknown" : deviceType.name());
            auditLog.setClientIp(requestIpAddress);
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