package cn.piesat.nj.slardar.starter;

import cn.piesat.nj.slardar.core.gateway.AccountGateway;
import cn.piesat.nj.slardar.core.gateway.UserProfileGateway;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * <p>
 * context
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/14
 */
public class SlardarContext implements ApplicationContextAware {

    private ApplicationContext context;

    /**
     * 用户信息 gateway
     * @return
     */
    public UserProfileGateway getUserGateway() {
        return getBean(UserProfileGateway.class);
    }


    public AccountGateway getAccountGateway() {
        return getBean(AccountGateway.class);
    }


    public <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }


    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        context = ctx;
    }
}
