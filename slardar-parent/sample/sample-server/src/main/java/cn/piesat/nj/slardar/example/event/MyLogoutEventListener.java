package cn.piesat.nj.slardar.example.event;

import cn.piesat.nj.slardar.core.event.SlardarEventListener;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.core.entity.Account;
import cn.piesat.nj.slardar.starter.support.event.LogoutEvent;
import org.springframework.stereotype.Component;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/6/24
 */
@Component
public class MyLogoutEventListener implements SlardarEventListener<LogoutEvent> {

    /**
     * 监听 /logout 事件 并处理
     *
     * @param event
     * @throws SlardarException
     */
    @Override
    public void onEvent(LogoutEvent event) throws SlardarException {
        // TODO: 这里应用方处理
        Account account = event.payload().getAccount();
        // ....
        System.out.println(account.getName());

    }

    /**
     * support event or not
     *
     * @param eventClass
     * @return
     */
    @Override
    public boolean support(Class<LogoutEvent> eventClass) {
        return LogoutEvent.class.equals(eventClass);
    }
}
