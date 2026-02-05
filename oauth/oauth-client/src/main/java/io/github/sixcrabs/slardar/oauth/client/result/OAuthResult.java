package io.github.sixcrabs.slardar.oauth.client.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.github.sixcrabs.slardar.oauth.client.OAuthException;

import java.io.Serializable;

/**
 * <p>
 * 授权结果
 * </p>
 *
 * @author Alex
 * @since 2025/12/5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthResult<T> implements Serializable {

    /**
     * 授权响应状态码
     */
    private int code;

    /**
     * 授权响应信息
     */
    private String msg;

    /**
     * 授权响应数据
     */
    private T data;

    public static <T> OAuthResult<T> success(T data) {
        return OAuthResult.<T>builder().code(OAuthResultStatus.SUCCESS.getCode()).data(data).build();
    }

    public static <T> OAuthResult<T> success() {
        return OAuthResult.<T>builder().code(OAuthResultStatus.SUCCESS.getCode()).msg(OAuthResultStatus.SUCCESS.getMsg()).data(null).build();
    }

    public static <T> OAuthResult<T> error(int code, String msg) {
        return OAuthResult.<T>builder().code(code).msg(msg).build();
    }

    public static <T> OAuthResult<T> error(String msg) {
        return OAuthResult.<T>builder().code(OAuthResultStatus.FAILURE.getCode()).msg(msg).build();
    }

    public static <T> OAuthResult<T> error(Exception exception) {
        if (exception instanceof OAuthException) {
            return OAuthResult.<T>builder()
                    .code(((OAuthException) exception).getErrorCode())
                    .msg(((OAuthException) exception).getErrorMsg()).build();
        } else
            return OAuthResult.<T>builder()
                    .code(OAuthResultStatus.FAILURE.getCode())
                    .msg(exception.getMessage()).build();
    }
}