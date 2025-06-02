package cn.piesat.v.slardar.oauth.server.support;

import cn.piesat.v.misc.hutool.mini.MapUtil;

import java.util.HashMap;

import static cn.piesat.v.slardar.oauth.server.support.OauthServerUtil.GSON;


/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
public class OauthServerException extends Exception {

    private int code;

    public OauthServerException() {
    }

    public OauthServerException(String message) {
        super(message);
    }

    public OauthServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public OauthServerException(Throwable cause) {
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

    public OauthServerException setCode(int code) {
        this.code = code;
        return this;
    }



}
