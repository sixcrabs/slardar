package org.winterfell.slardar.starter.support;

import org.winterfell.slardar.captcha.generator.RandomGenerator;
import org.winterfell.slardar.captcha.impl.LineCaptcha;
import org.winterfell.slardar.captcha.support.CaptchaUtil;
import org.winterfell.slardar.spi.SlardarKeyStore;
import org.winterfell.slardar.spi.SlardarSpiFactory;
import org.winterfell.slardar.starter.config.SlardarProperties;
import cn.piesat.v.misc.hutool.mini.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * <p>
 * 获取验证码 验证 删除等
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/26
 */
@Component
public class CaptchaComponent {

    private final SlardarKeyStore keyStore;

    private final SlardarProperties.CaptchaSetting settings;

    public static final String CAPTCHA_KEY_PREFIX = "CaptchaCode_";

    public static final Logger log = LoggerFactory.getLogger(CaptchaComponent.class);

    public CaptchaComponent(SlardarSpiFactory spiFactory, SlardarProperties slardarProperties) {
        this.keyStore = spiFactory.findKeyStore(slardarProperties.getKeyStore().getType());
        this.settings = slardarProperties.getCaptcha();
    }

    /**
     * 指定图片大小生成
     * 默认5分钟有效
     *
     * @param width
     * @param height
     * @param sessionId
     * @return
     */
    public CaptchaPayload generate(int width, int height, String sessionId) {
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(width, height);
        // TODO: 这里支持多种验证码选项 留给 SPI 配置参数控制
        RandomGenerator randomGenerator = new RandomGenerator(settings.getRandomBase(), settings.getLength());
//        MathGenerator mathGenerator = new MathGenerator(1);
        lineCaptcha.setGenerator(randomGenerator);
        //  验证码值
        String code = lineCaptcha.getCode();
        CaptchaPayload payload = new CaptchaPayload();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            lineCaptcha.write(baos);
            boolean saved = keyStore.setex(CAPTCHA_KEY_PREFIX.concat(sessionId), code, settings.getExpiration());
            if (saved) {
                payload.setCode(code)
                        .setImgBytes(baos.toByteArray())
                        .setSessionId(sessionId);
            }
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
        }
        return payload;
    }

    /**
     * 使用默认配置生成
     *
     * @param sessionId
     * @return
     */
    public CaptchaPayload generate(String sessionId) {
        return generate(settings.getWidth(), settings.getHeight(), sessionId);
    }

    /**
     * 验证
     *
     * @param sessionId session id
     * @param code      请求中的 验证码
     * @return
     */
    public boolean verify(String sessionId, String code) {
        String captchaCode = keyStore.get(CAPTCHA_KEY_PREFIX.concat(sessionId));
        if (StringUtil.isBlank(captchaCode)) {
            // 已过期或不存在
            return false;
        }
        boolean b = code.equalsIgnoreCase(captchaCode);
        // 立即过期
        keyStore.setex(CAPTCHA_KEY_PREFIX.concat(sessionId), "", 1L);
        return b;
    }


    public static class CaptchaPayload implements Serializable {

        private String sessionId;

        private String code;

        private byte[] imgBytes;

        public String getSessionId() {
            return sessionId;
        }

        public CaptchaPayload setSessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public String getCode() {
            return code;
        }

        public CaptchaPayload setCode(String code) {
            this.code = code;
            return this;
        }

        public byte[] getImgBytes() {
            return imgBytes;
        }

        public CaptchaPayload setImgBytes(byte[] imgBytes) {
            this.imgBytes = imgBytes;
            return this;
        }
    }


}
