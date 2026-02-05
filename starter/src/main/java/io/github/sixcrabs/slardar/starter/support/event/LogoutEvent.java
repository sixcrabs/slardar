package io.github.sixcrabs.slardar.starter.support.event;

import io.github.sixcrabs.slardar.core.domain.Account;

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

    public LogoutEvent(Account account,HttpServletRequest request) {
        super(new Payload().setAccount(account).setRequest(request));
    }

    public static class Payload implements Serializable {

        private Account account;

        private HttpServletRequest request;

        public HttpServletRequest getRequest() {
            return request;
        }

        public Payload setRequest(HttpServletRequest request) {
            this.request = request;
            return this;
        }

        public Account getAccount() {
            return account;
        }

        public Payload setAccount(Account account) {
            this.account = account;
            return this;
        }
    }



}