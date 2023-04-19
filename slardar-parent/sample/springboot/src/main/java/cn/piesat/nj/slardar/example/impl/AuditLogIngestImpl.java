package cn.piesat.nj.slardar.example.impl;

import cn.piesat.nj.slardar.core.AuditLogIngest;
import cn.piesat.nj.slardar.core.entity.AuditLog;
import cn.piesat.nj.slardar.core.entity.Role;
import cn.piesat.nj.slardar.core.gateway.AuditLogGateway;
import cn.piesat.nj.slardar.starter.support.SecUtil;
import org.springframework.stereotype.Component;

import java.util.List;

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
        System.out.println(auditLog);

    }
}
