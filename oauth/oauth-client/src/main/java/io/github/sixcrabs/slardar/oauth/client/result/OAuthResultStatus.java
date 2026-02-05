package io.github.sixcrabs.slardar.oauth.client.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * result status
 * </p>
 *
 * @author Alex
 * @since 2025/12/5
 */
@Getter
@AllArgsConstructor
public enum OAuthResultStatus {

    SUCCESS(0, "Success"),
    FAILURE(5000, "Failure, unknown error"),
    NOT_IMPLEMENTED(5001, "Not Implemented"),
    PARAMETER_INCOMPLETE(5002, "Parameter incomplete"),
    UNSUPPORTED(5003, "Unsupported operation"),
    UNIDENTIFIED_PLATFORM(5005, "Unidentified platform"),
    ILLEGAL_REDIRECT_URI(5006, "Illegal redirect uri"),
    ILLEGAL_REQUEST(5007, "Illegal request"),
    ILLEGAL_CODE(5008, "Illegal code"),
    ILLEGAL_STATE(5009, "Illegal state"),
    REQUIRED_REFRESH_TOKEN(5010, "The refresh token is required; it must not be null"),
    ILLEGAL_TOKEN(5011, "Invalid token"),
    ILLEGAL_CLIENT_ID(5014, "Invalid client id"),
    ILLEGAL_CLIENT_SECRET(5015, "Invalid client secret");

    private final int code;
    private final String msg;
}