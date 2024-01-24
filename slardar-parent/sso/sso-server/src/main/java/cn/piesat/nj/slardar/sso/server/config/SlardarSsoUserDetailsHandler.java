package cn.piesat.nj.slardar.sso.server.config;

import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.sso.server.support.SsoException;
import cn.piesat.nj.slardar.starter.SlardarUserDetails;

import java.io.Serializable;

/**
 * <p>
 * 自定义处理 sso 返回的用户详情
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/1/24
 */
@FunctionalInterface
public interface SlardarSsoUserDetailsHandler {

    /**
     * 处理用户详情，如: 屏蔽密码 或修改返回结构等
     * @param details
     * @return
     * @throws SlardarException
     */
    Serializable handle(SlardarUserDetails details) throws SsoException;
}
