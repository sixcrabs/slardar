package cn.piesat.nj.slardar.starter.support.event;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/4/3
 */
public class LogoutEvent extends BaseSlardarEvent<LogoutEvent.Payload>{

    public LogoutEvent(Payload data) {
        super(data);
    }

    public LogoutEvent(String name,HttpServletRequest request) {
        super(new Payload().setAccountName(name).setRequest(request));
    }

    public static class Payload implements Serializable {

        private String accountName;
        private HttpServletRequest request;

        public HttpServletRequest getRequest() {
            return request;
        }

        public Payload setRequest(HttpServletRequest request) {
            this.request = request;
            return this;
        }

        public String getAccountName() {
            return accountName;
        }

        public Payload setAccountName(String accountName) {
            this.accountName = accountName;
            return this;
        }
    }



}
