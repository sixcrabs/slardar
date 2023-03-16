package cn.piesat.nj.slardar.example.impl;

import cn.piesat.nj.slardar.core.entity.AuditLog;
import cn.piesat.nj.slardar.core.gateway.AuditLogGateway;
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
public class AuditLogGatewayImpl implements AuditLogGateway {

    /**
     * 创建实体
     *
     * @param entity
     * @return
     */
    @Override
    public String create(AuditLog entity) {
        System.out.println(entity.toString());
        return null;
    }

    /**
     * 更新实体
     *
     * @param entity
     * @return
     */
    @Override
    public boolean update(AuditLog entity) {
        return false;
    }

    /**
     * delete by ID
     *
     * @param s
     * @return
     */
    @Override
    public boolean deleteById(String s) {
        return false;
    }

    /**
     * get by id
     *
     * @param s
     * @return
     */
    @Override
    public AuditLog getById(String s) {
        return null;
    }
}
