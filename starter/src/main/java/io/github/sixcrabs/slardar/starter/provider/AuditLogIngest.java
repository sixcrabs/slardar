package io.github.sixcrabs.slardar.starter.provider;

import io.github.sixcrabs.slardar.core.domain.AuditLog;

/**
 * <p>
 * ingest audit log
 * </p>
 *
 * @author alex
 * @version v1.0 2023/4/19
 */
public interface AuditLogIngest {

    /**
     * ingest log
     * @param auditLog
     */
    void ingest(AuditLog auditLog);
}