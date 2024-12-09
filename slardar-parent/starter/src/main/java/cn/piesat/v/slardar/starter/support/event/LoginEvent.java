package cn.piesat.v.slardar.starter.support.event;

import cn.piesat.v.slardar.core.entity.Account;
import cn.piesat.v.slardar.starter.authenticate.SlardarAuthentication;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>
 * .
 * </p>
 *
 * @author alex
 * @version v1.0 2023/4/2
 */
public class LoginEvent extends BaseSlardarEvent<LoginEvent.LoginEventPayload> {


    public LoginEvent(LoginEventPayload data) {
        super(data);
    }

    public LoginEvent(Account account, boolean succeed) {
        super(new LoginEventPayload().setAccount(account).setState(succeed ? 0 : -1).setCreateAt(LocalDateTime.now()));
    }

    public LoginEvent(Account account, boolean succeed, final Map headers, final SlardarAuthentication authentication) {
        super(new LoginEventPayload()
                .setAccount(account)
                .setState(succeed ? 0 : -1)
                .setCreateAt(LocalDateTime.now())
                .setRequestHeaders(headers)
                .setAuthentication(authentication));
    }

    public LoginEvent(SlardarAuthentication authentication, Map headers, Exception ex) {
        super(new LoginEventPayload()
                .setAccount(null)
                .setState(-1)
                .setRequestHeaders(headers)
                .setCreateAt(LocalDateTime.now())
                .setAuthentication(authentication).setExMessage(ex.getLocalizedMessage()));
    }


    public static class LoginEventPayload implements Serializable {

        /**
         * 状态 0 : 成功登录
         */
        private int state = 0;

        /**
         * 登录的账号登信息
         */
        private Account account;

        /**
         * 记录时间
         */
        private LocalDateTime createAt;

        private SlardarAuthentication authentication;

        private String exMessage;

        private Map requestHeaders;

        public Map getRequestHeaders() {
            return requestHeaders;
        }

        public LoginEventPayload setRequestHeaders(Map requestHeaders) {
            this.requestHeaders = requestHeaders;
            return this;
        }

        public SlardarAuthentication getAuthentication() {
            return authentication;
        }

        public LoginEventPayload setAuthentication(SlardarAuthentication authentication) {
            this.authentication = authentication;
            return this;
        }

        public String getExMessage() {
            return exMessage;
        }

        public LoginEventPayload setExMessage(String exMessage) {
            this.exMessage = exMessage;
            return this;
        }

        public boolean isFailed() {
            return state != 0;
        }

        public boolean isSucceed() {
            return state == 0;
        }

        public int getState() {
            return state;
        }

        public LoginEventPayload setState(int state) {
            this.state = state;
            return this;
        }

        public Account getAccount() {
            return account;
        }

        public LoginEventPayload setAccount(Account account) {
            this.account = account;
            return this;
        }

        public LocalDateTime getCreateAt() {
            return createAt;
        }

        public LoginEventPayload setCreateAt(LocalDateTime createAt) {
            this.createAt = createAt;
            return this;
        }
    }
}
