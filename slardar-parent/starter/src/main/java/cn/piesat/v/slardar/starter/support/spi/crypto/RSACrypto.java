package cn.piesat.v.slardar.starter.support.spi.crypto;

import cn.piesat.v.misc.hutool.mini.StringUtil;
import cn.piesat.v.misc.hutool.mini.crypto.SecureUtil;
import cn.piesat.v.misc.hutool.mini.crypto.asymmetric.KeyType;
import cn.piesat.v.misc.hutool.mini.crypto.asymmetric.RSA;
import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.spi.crypto.SlardarCrypto;
import cn.piesat.v.slardar.starter.config.SlardarProperties;
import com.google.auto.service.AutoService;
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
    public void initialize(SlardarSpiContext context) {
        SlardarProperties properties = context.getBeanIfAvailable(SlardarProperties.class);
        SlardarProperties.EncryptSetting encrypt = properties.getLogin().getEncrypt();
        String publicKey = encrypt.getMode().equalsIgnoreCase(MODE) ? encrypt.getSecretKey() : null;
        if (StringUtil.isBlank(publicKey)) {
            publicKey = "ab0c1de2fg3hi4jk5l1n9";
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
