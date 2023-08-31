package cn.piesat.nj.slardar.example.config;

import cn.piesat.nj.slardar.core.SlardarEventListener;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.support.event.LoginEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/4/14
 */
@Component
public class MyEventListener implements SlardarEventListener<LoginEvent> {

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
        HttpServletRequest request = payload.getRequest();

        String openId = request.getHeader("open-id");
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
