package cn.piesat.nj.slardar.starter.support.event;

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

    public LogoutEvent(String name) {
        super(new Payload().setAccountName(name));
    }

    public static class Payload implements Serializable {

        private String accountName;


        public String getAccountName() {
            return accountName;
        }

        public Payload setAccountName(String accountName) {
            this.accountName = accountName;
            return this;
        }
    }



}
