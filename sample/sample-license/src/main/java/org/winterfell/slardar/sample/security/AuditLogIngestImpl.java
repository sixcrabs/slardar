package org.winterfell.slardar.sample.security;

import io.github.sixcrabs.slardar.starter.provider.AuditLogIngest;
import io.github.sixcrabs.slardar.core.domain.AuditLog;
import org.springframework.stereotype.Component;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/16
 */
@Component
public class AuditLogIngestImpl implements AuditLogIngest {

    /**
     * ingest log
     *
     * @param auditLog
     */
    @Override
    public void ingest(AuditLog auditLog) {
        // 这里入日志库或入到消息队列
        System.out.println(auditLog);
        System.out.println(auditLog.getDetail());
    }
}