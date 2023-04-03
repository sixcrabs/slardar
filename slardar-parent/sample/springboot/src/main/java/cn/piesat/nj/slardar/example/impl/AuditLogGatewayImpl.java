package cn.piesat.nj.slardar.example.impl;

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
public class AuditLogGatewayImpl implements AuditLogGateway {


    @Override
    public String create(AuditLog entity) throws Exception {


        return null;
    }



    @Override
    public boolean update(AuditLog entity) throws Exception {
        return false;
    }

    @Override
    public boolean deleteById(String s) throws Exception {
        return false;
    }

    @Override
    public AuditLog getById(String s) throws Exception {
        return null;
    }
}
