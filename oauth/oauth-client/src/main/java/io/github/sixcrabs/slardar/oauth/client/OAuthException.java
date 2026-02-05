package io.github.sixcrabs.slardar.oauth.client;

import lombok.Getter;
import io.github.sixcrabs.slardar.oauth.client.result.OAuthResultStatus;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/12/5
 */
@Getter
public class OAuthException extends RuntimeException {

    private int errorCode;
    private String errorMsg;

    public OAuthException(int errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public OAuthException(String errorMsg) {
        this(OAuthResultStatus.FAILURE.getCode(), errorMsg);
    }

    public OAuthException(OAuthResultStatus status) {
        this(status.getCode(), status.getMsg());
    }

    public OAuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public OAuthException(Throwable cause) {
        super(cause);
    }
}