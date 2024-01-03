package cn.piesat.nj.slardar.example.config;

import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.authenticate.SlardarAuthenticatePreHandler;
import cn.piesat.nj.slardar.starter.support.LoginDeviceType;
import cn.piesat.nj.slardar.starter.authenticate.SlardarAuthentication;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 演示在登录之前 做前置处理
 * 如 阻止某些账号登录， ip 黑白名单，判断登录端来源等
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/4/14
 */
//@Component
public class MySlardarAuthenticatePreHandler implements SlardarAuthenticatePreHandler {

    /**
     * 在进入认证前 由应用前置处理，
     * 如
     * - 判断登录端类型
     * - 判断客户端ip等
     *
     * @param authentication
     * @throws SlardarException 抛出异常 则终止认证
     */
    @Override
    public void preHandle(SlardarAuthentication authentication) throws SlardarException {
        // 这里可以访问数据库进行一系列操作
        LoginDeviceType loginDeviceType = authentication.getLoginDeviceType();
        String accountName = authentication.getAccountName();
        if ("zhangsan".equals(accountName)) {
            if (loginDeviceType.equals(LoginDeviceType.APP)) {
                throw new SlardarException("账号【%s】不允许在 %s 登录", accountName, "APP");
            }
        }

    }
}
