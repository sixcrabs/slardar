package org.winterfell.slardar.starter.support;

import org.springframework.security.core.AuthenticationException;

/**
 * <p>
 * 自定义认证异常类
 * - 添加了认证账号名
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/8/19
 */
public class SlardarAuthenticationException extends AuthenticationException {

    /**
     * Constructs an {@code AuthenticationException} with the specified message and no
     * root cause.
     *
     * @param msg the detail message
     */
    public SlardarAuthenticationException(String msg, String accountName) {
        super(msg);
        this.accountName = accountName;
    }

    private final String accountName;

    public String getAccountName() {
        return accountName;
    }
}
