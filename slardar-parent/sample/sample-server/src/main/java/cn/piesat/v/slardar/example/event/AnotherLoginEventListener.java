package cn.piesat.v.slardar.example.event;

import cn.piesat.v.slardar.core.event.SlardarEventListener;
import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.starter.support.event.LoginEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <p>
 * .演示自定义处理 login 事件
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/4/14
 */
@Component
public class AnotherLoginEventListener implements SlardarEventListener<LoginEvent> {

    /**
     * 控制消费的次序 值越小越在前
     *
     * @return
     */
    @Override
    public int ordinal() {
        return 0;
    }

    /**
     * handle event
     *
     * @param event
     * @throws SlardarException
     */
    @Override
    public void onEvent(LoginEvent event) throws SlardarException {
        LoginEvent.LoginEventPayload payload = event.payload();
        // 登录成功后续操作
        Map headers = payload.getRequestHeaders();

        Object openId = headers.get("open-id");
        // 绑定 openid
        System.out.printf("openID: %s \r\n", openId);
    }

    /**
     * support event or not
     *
     * @param eventClass
     * @return
     */
    @Override
    public boolean support(Class<LoginEvent> eventClass) {
        return LoginEvent.class.equals(eventClass);
    }
}
