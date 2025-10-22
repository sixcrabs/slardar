package org.winterfell.slardar.example.config;

import org.winterfell.slardar.core.SlardarException;
import org.winterfell.slardar.starter.authenticate.SlardarAuthenticatePreHandler;
import org.winterfell.slardar.starter.authenticate.SlardarAuthentication;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * <p>
 * 演示ip白名单 登录拦截
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/12/4
 */
@Component
public class IpWhiteListSlardarAuthenticatePreHandler implements SlardarAuthenticatePreHandler {

    private static final  ArrayList<String> ipList = Lists.newArrayList("127.0.0.1");

    /**
     * 在进入认证前 由应用前置处理，
     * 如
     * - 判断登录端类型
     * - 判断客户端ip等
     *
     * @param authentication 认证数据对象
     * @throws SlardarException 抛出异常 则终止认证
     */
    @Override
    public void preHandle(SlardarAuthentication authentication) throws SlardarException {
        String clientIp = authentication.getReqClientIp();
        // 这里进行ip 白名单控制
        System.out.println(clientIp);
    }
}
