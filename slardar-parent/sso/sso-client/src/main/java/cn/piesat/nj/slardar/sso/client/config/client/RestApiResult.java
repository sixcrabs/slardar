package cn.piesat.nj.slardar.sso.client.config.client;

import java.io.Serializable;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/4/3
 */
public class RestApiResult<T> implements Serializable {

    private int code;

    private String message;

    private T data;

    public boolean isSuccessful() {
        return this.code <= 200;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
