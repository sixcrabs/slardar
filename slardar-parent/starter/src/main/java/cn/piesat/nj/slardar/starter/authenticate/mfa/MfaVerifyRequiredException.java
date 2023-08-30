package cn.piesat.nj.slardar.starter.authenticate.mfa;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;

/**
 * <p>
 * 需要进行 MFA 验证
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/29
 */
public class MfaVerifyRequiredException extends AuthenticationServiceException {

    private final String key;

    public String getKey() {
        return key;
    }

    /**
     * Constructs an {@code AuthenticationException} with the specified message and root
     * cause.
     *  @param msg the detail message
     * @param t   the root cause
     * @param key
     */
    public MfaVerifyRequiredException(String msg, Throwable t, String key) {
        super(msg, t);
        this.key = key;
    }

    /**
     * Constructs an {@code AuthenticationException} with the specified message and no
     * root cause.
     *
     * @param msg the detail message
     * @param key
     */
    public MfaVerifyRequiredException(String msg, String key) {
        super(msg);
        this.key = key;
    }
}
