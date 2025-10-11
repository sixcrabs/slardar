package org.winterfell.slardar.core;

import org.winterfell.slardar.core.entity.AuditLog;

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
