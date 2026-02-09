package io.github.sixcrabs.slardar.starter.support.spi.crypto;

import io.github.sixcrabs.slardar.core.SlardarException;
import io.github.sixcrabs.slardar.core.SlardarContext;
import io.github.sixcrabs.slardar.spi.crypto.SlardarCrypto;
import io.github.sixcrabs.slardar.starter.SlardarProperties;
import com.google.auto.service.AutoService;
import io.github.sixcrabs.winterfell.mini.StringUtil;
import io.github.sixcrabs.winterfell.mini.crypto.SecureUtil;
import io.github.sixcrabs.winterfell.mini.crypto.asymmetric.KeyType;
import io.github.sixcrabs.winterfell.mini.crypto.asymmetric.RSA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * <p>
 * RSA 非对称加密
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/10/11
 */
@AutoService(SlardarCrypto.class)
public class RSACrypto implements SlardarCrypto {

    private static final Logger logger = LoggerFactory.getLogger(RSACrypto.class);

    public static final String MODE = "RSA";

    private static final String rsaPrivateKey = "whosyourdaddy";

    private RSA rsa;

    /**
     * 实现名称, 区分不同的 SPI 实现，必须
     *
     * @return
     */
    @Override
    public String name() {
        return MODE;
    }

    /**
     * set context
     *
     * @param context
     */
    @Override
    public void initialize(SlardarContext context) {
        SlardarProperties properties = context.getBeanIfAvailable(SlardarProperties.class);
        SlardarProperties.EncryptSetting encrypt = properties.getLogin().getEncrypt();
        String publicKey = encrypt.getMode().equalsIgnoreCase(MODE) ? encrypt.getSecretKey() : null;
        if (StringUtil.isBlank(publicKey)) {
            publicKey = "aK0hD0jP5";
        }
        try {
            rsa = SecureUtil.rsa(rsaPrivateKey, publicKey);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    /**
     * 加密
     *
     * @param plaintext
     * @return
     */
    @Override
    public String encrypt(String plaintext) throws SlardarException {
        byte[] bytes = rsa.encrypt(plaintext, StandardCharsets.UTF_8, KeyType.PublicKey);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 解密
     *
     * @param ciphertext
     * @return
     */
    @Override
    public String decrypt(String ciphertext) throws SlardarException {
        try {
            return rsa.decryptStr(ciphertext, KeyType.PrivateKey, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SlardarException(e);
        }
    }
}