package cn.piesat.v.slardar.sso.server.support;

import cn.hutool.core.map.MapUtil;

import java.util.HashMap;

import static cn.piesat.v.slardar.sso.server.support.SsoConstants.GSON;


/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
public class SsoException extends Exception {

    private int code;

    public SsoException() {
    }

    public SsoException(String message) {
        super(message);
    }

    public SsoException(String message, Throwable cause) {
        super(message, cause);
    }

    public SsoException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return this.buildMessage();
    }

    @Override
    public String getLocalizedMessage() {
        return this.buildMessage();
    }

    private String buildMessage() {
        HashMap<String, Object> map = MapUtil.of("code", code);
        map.put("message", this.getCause() != null ? this.getCause().getLocalizedMessage() : super.getMessage());
        return GSON.toJson(map);
    }

    public String getCauseMessage() {
        return this.getCause() != null ? this.getCause().getLocalizedMessage() : super.getMessage();
    }

    public int getCode() {
        return code;
    }

    public SsoException setCode(int code) {
        this.code = code;
        return this;
    }



}
