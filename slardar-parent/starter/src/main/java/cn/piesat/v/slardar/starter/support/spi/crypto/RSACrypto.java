package cn.piesat.v.slardar.starter.support.spi.crypto;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.piesat.v.misc.hutool.mini.StringUtil;
import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.spi.crypto.SlardarCrypto;
import cn.piesat.v.slardar.starter.config.SlardarProperties;
import com.google.auto.service.AutoService;

import java.nio.charset.StandardCharsets;

/**
 * <p>
 * TESTME:
 * RSA 非对称加密
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/10/11
 */
@AutoService(SlardarCrypto.class)
public class RSACrypto implements SlardarCrypto {

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
        String publicKey = properties.getLogin().getEncrypt().getSecretKey();
        if (StringUtil.isBlank(publicKey)) {
            publicKey = "ab0c1de2fg3hi4jk5l1n9";
        }
        rsa = new RSA(rsaPrivateKey, publicKey);
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
        return rsa.decryptStr(ciphertext, KeyType.PrivateKey, StandardCharsets.UTF_8);
    }
}
