package cn.piesat.nj.slardar.starter.support.event;

import cn.piesat.nj.slardar.core.SlardarEvent;
import cn.piesat.nj.slardar.core.entity.Account;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.time.LocalDateTime;

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

    public LoginEvent(Account account, boolean succeed, HttpServletRequest request) {
        super(new LoginEventPayload().setAccount(account).setState(succeed ? 0 : -1).setCreateAt(LocalDateTime.now())
                .setRequest(request));
    }

    public LoginEvent(HttpServletRequest request, Exception ex) {
        super(new LoginEventPayload().setAccount(null).setState(-1).setCreateAt(LocalDateTime.now())
                .setRequest(request).setExMessage(ex.getLocalizedMessage()));
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

        private HttpServletRequest request;

        private String exMessage;

        public String getExMessage() {
            return exMessage;
        }

        public LoginEventPayload setExMessage(String exMessage) {
            this.exMessage = exMessage;
            return this;
        }

        public HttpServletRequest getRequest() {
            return request;
        }

        public LoginEventPayload setRequest(HttpServletRequest request) {
            this.request = request;
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
