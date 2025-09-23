package cn.piesat.v.slardar.license.manager;

import cn.piesat.v.misc.hutool.mini.MapUtil;
import cn.piesat.v.slardar.license.manager.config.SlardarLicenseProperties;
import cn.piesat.v.slardar.spi.SlardarKeyStore;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.spi.SlardarSpiFactory;
import cn.piesat.v.slardar.starter.config.SlardarProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static cn.piesat.v.slardar.starter.support.SecUtil.GSON;

/**
 * <p>
 * 处理 license/* 请求
 * </p>
 *
 * @author Alex
 * @since 2025/9/23
 */
public class LicenseManageRequestHandler implements InitializingBean {

    public static final Logger logger = LoggerFactory.getLogger(LicenseManageRequestHandler.class);

    private final SlardarLicenseProperties licenseProperties;

    private final SlardarKeyStore keyStore;

    public LicenseManageRequestHandler(SlardarLicenseProperties licenseProperties, SlardarProperties slardarProperties,
                                       SlardarSpiFactory spiFactory) {
        this.licenseProperties = licenseProperties;
        this.keyStore = spiFactory.findKeyStore(slardarProperties.getKeyStore().getType());
    }

    public void handle(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        String mapping = uri.replace("/license", "").replaceFirst("/", "");
        LicenseRequestMapping requestMapping = LicenseRequestMapping.valueOf(mapping);
        switch (requestMapping) {
            case status:
                // TODO:
                break;
            case install:
                // TODO:
                break;
            case uninstall:
                // TODO:
                break;
            default:
                break;
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // TODO: 检测store中是否有许可信息 如果没有 则立即install


    }

    /**
     * send json to response
     *
     * @param response
     * @param result
     * @throws IOException
     */
    private void sendJson(HttpServletResponse response, Serializable result, HttpStatus httpStatus) {
        response.setStatus(httpStatus.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try {
            response.getWriter().write((result instanceof String) ? result.toString() : GSON.toJson(result));
            response.getWriter().flush();
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    private HashMap<String, Object> makeResult(Object result, int code) {
        HashMap<String, Object> ret = MapUtil.of("code", code);
        ret.put("data", result);
        return ret;
    }

    private HashMap<String, Object> makeErrorResult(String msg, int code) {
        HashMap<String, Object> ret = MapUtil.of("code", code);
        ret.put("data", null);
        ret.put("message", msg);
        return ret;
    }

}
