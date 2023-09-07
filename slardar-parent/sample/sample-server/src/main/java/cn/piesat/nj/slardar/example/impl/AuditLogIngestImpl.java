package cn.piesat.nj.slardar.example.impl;

import cn.piesat.nj.slardar.core.AuditLogIngest;
import cn.piesat.nj.slardar.core.entity.AuditLog;
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
        // 这里入日志库或入到消息队列
        System.out.println(auditLog);
    }
}
