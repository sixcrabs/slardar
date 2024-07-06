package cn.piesat.nj.slardar.starter.support.event.listener;

import cn.piesat.nj.slardar.core.AuditLogIngest;
import cn.piesat.nj.slardar.core.SlardarEventListener;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.core.entity.Account;
import cn.piesat.nj.slardar.core.entity.AuditLog;
import cn.piesat.nj.slardar.core.entity.UserProfile;
import cn.piesat.nj.slardar.spi.SlardarSpiContext;
import cn.piesat.nj.slardar.starter.support.event.LoginEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

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

    private static final Logger log = LoggerFactory.getLogger(LoginEventListener.class);

    private final AuditLogIngest auditLogIngest;

    public LoginEventListener(SlardarSpiContext slardarContext) {
        this.auditLogIngest = slardarContext.getAuditLogIngest();
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
            Account account = payload.getAccount();
            AuditLog auditLog = new AuditLog()
                    .setClientIp(payload.getAuthentication().getReqClientIp())
                    .setClientType(payload.getAuthentication().getLoginDeviceType().name())
                    .setLogType("登录")
                    .setLogTime(LocalDateTime.now());

            if (Objects.isNull(account)) {
                if (auditLogIngest != null) {
                    auditLogIngest.ingest(auditLog.setDetail(payload.getExMessage()));
                } else {
                    log.error("`AuditLogIngest` is null");
                }
            } else {
                String id = account.getId();
                if (auditLogIngest != null) {
                    UserProfile userProfile = account.getUserProfile();
                    if (userProfile != null) {
                        auditLog.setUserProfileId(userProfile.getId())
                                .setUserProfileName(userProfile.getName());
                    }
                    auditLog.setAccountId(id)
                            .setDetail("登录成功")
                            .setAccountName(account.getName())
                            .setRealm(account.getRealm());
                    auditLog.setAccountName(account.getName());
                    auditLogIngest.ingest(auditLog);
                } else {
                    log.error("`AuditLogIngest` is null");
                }
            }


        } catch (Exception e) {
            throw new SlardarException(e);
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
        return LoginEvent.class.equals(eventClass);
    }
}
