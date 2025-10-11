package org.winterfell.slardar.starter.support.spi.crypto;

import cn.piesat.v.misc.hutool.mini.crypto.Mode;
import cn.piesat.v.misc.hutool.mini.crypto.Padding;
import cn.piesat.v.misc.hutool.mini.crypto.SecureUtil;
import cn.piesat.v.misc.hutool.mini.crypto.symmetric.AES;
import org.winterfell.slardar.core.SlardarException;
import org.winterfell.slardar.spi.SlardarSpiContext;
import org.winterfell.slardar.spi.crypto.SlardarCrypto;
import org.winterfell.slardar.starter.config.SlardarProperties;
import cn.piesat.v.misc.hutool.mini.StringUtil;
import com.google.auto.service.AutoService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.charset.StandardCharsets;
import java.security.Security;

/**
 * <p>
 * AES 对称加解密
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/28
 */
@AutoService(SlardarCrypto.class)
public class AESCrypto implements SlardarCrypto {

    public static final String MODE = "AES";

    private AES aes = SecureUtil.aes();

    static {
        // hack
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 加密模式
     * aes/sm3/sm4/...
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
        String secretKey = encrypt.getMode().equalsIgnoreCase(MODE) ? encrypt.getSecretKey() : StringUtil.EMPTY;
        String defaultSecretKey = "ab0c1de2fg3hi4jk5lmnopqrstuvwxyz";
        aes = new AES(Mode.ECB, Padding.ISO10126Padding, StringUtil.isNotBlank(secretKey) ? secretKey.getBytes() : defaultSecretKey.getBytes());
    }

    /**
     * 加密
     *
     * @param plaintext
     * @return
     */
    @Override
    public String encrypt(String plaintext) throws SlardarException {
        return aes.encryptBase64(plaintext);
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
            return aes.decryptStr(ciphertext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SlardarException(e);
        }
    }
}
