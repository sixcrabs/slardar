package io.github.sixcrabs.slardar.starter.support;

import org.winterfell.misc.hutool.mini.StringUtil;
import io.github.sixcrabs.slardar.captcha.generator.CodeGenerator;
import io.github.sixcrabs.slardar.captcha.generator.MathGenerator;
import io.github.sixcrabs.slardar.captcha.generator.RandomGenerator;
import io.github.sixcrabs.slardar.captcha.impl.AbstractCaptcha;
import io.github.sixcrabs.slardar.captcha.support.CaptchaUtil;
import io.github.sixcrabs.slardar.spi.SlardarKeyStore;
import io.github.sixcrabs.slardar.spi.SlardarSpiFactory;
import io.github.sixcrabs.slardar.starter.SlardarProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import static io.github.sixcrabs.slardar.core.Constants.KEY_PREFIX_CAPTCHA;

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
        AbstractCaptcha captcha = settings.isGif() ? CaptchaUtil.createGifCaptcha(width, height) : CaptchaUtil.createLineCaptcha(width, height);
        CodeGenerator codeGenerator = settings.getCodeType().equalsIgnoreCase("math") ? new MathGenerator(1) :
                new RandomGenerator(settings.getRandomBase(), settings.getCodeLength());
        captcha.setGenerator(codeGenerator);
        String code = captcha.getCode();
        CaptchaPayload payload = new CaptchaPayload();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            captcha.write(baos);
            boolean saved = keyStore.setex(KEY_PREFIX_CAPTCHA.concat(sessionId), code, settings.getExpiration());
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
        String captchaCode = keyStore.get(KEY_PREFIX_CAPTCHA.concat(sessionId));
        if (StringUtil.isBlank(captchaCode)) {
            // 已过期或不存在
            return false;
        }
        boolean b = code.equalsIgnoreCase(captchaCode);
        // 立即过期
        keyStore.setex(KEY_PREFIX_CAPTCHA.concat(sessionId), "", 1L);
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