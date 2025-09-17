package cn.piesat.v.slardar.starter.support.event.listener;

import cn.piesat.v.slardar.core.AuditLogIngest;
import cn.piesat.v.slardar.core.event.SlardarEventListener;
import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.core.entity.AuditLog;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.starter.support.HttpServletUtil;
import cn.piesat.v.slardar.starter.support.LoginDeviceType;
import cn.piesat.v.slardar.starter.support.event.LogoutEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Objects;

import static cn.piesat.v.slardar.starter.support.HttpServletUtil.getDeviceType;

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

    public LogoutEventListener(SlardarSpiContext slardarContext) {
        this.auditLogIngest = slardarContext.getAuditLogIngest();
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
