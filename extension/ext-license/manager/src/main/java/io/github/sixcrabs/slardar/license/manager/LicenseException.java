package io.github.sixcrabs.slardar.license.manager;

import com.google.gson.Gson;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.winterfell.misc.hutool.mini.MapUtil;

import java.util.HashMap;


/**
 * <p>
 * exception
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
public class LicenseException extends Exception {

    private static final Gson GSON = GsonBuilderUtils.gsonBuilderWithBase64EncodedByteArrays().create();

    private int code;

    public LicenseException() {
    }

    public LicenseException(String message) {
        super(message);
    }

    public LicenseException(String message, int code) {
        super(message);
        this.code = code;
    }

    public LicenseException(String message, Throwable cause) {
        super(message, cause);
    }

    public LicenseException(Throwable cause) {
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

    public LicenseException setCode(int code) {
        this.code = code;
        return this;
    }


}