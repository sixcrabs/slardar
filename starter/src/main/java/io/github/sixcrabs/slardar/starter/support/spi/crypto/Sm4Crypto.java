package io.github.sixcrabs.slardar.starter.support.spi.crypto;

import io.github.sixcrabs.slardar.core.SlardarException;
import io.github.sixcrabs.slardar.core.SlardarContext;
import io.github.sixcrabs.slardar.spi.crypto.SlardarCrypto;
import io.github.sixcrabs.slardar.starter.SlardarProperties;
import com.google.auto.service.AutoService;
import io.github.sixcrabs.winterfell.mini.CharsetUtil;
import io.github.sixcrabs.winterfell.mini.HexUtil;
import io.github.sixcrabs.winterfell.mini.crypto.SmUtil;
import io.github.sixcrabs.winterfell.mini.crypto.symmetric.SM4;

/**
 * <p>
 * 国密4
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/28
 */
@AutoService(SlardarCrypto.class)
public class Sm4Crypto implements SlardarCrypto {

    public static final String MODE = "sm4";

    /**
     * 16 进制字符串，要求为 128 比特
     */
    private String key = "0a1b2c3d4e5f6f7e8d9cba9876543210";


    private SM4 sm4;

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
    public void initialize(SlardarContext context) {
        SlardarProperties properties = context.getBeanIfAvailable(SlardarProperties.class);
        SlardarProperties.EncryptSetting encrypt = properties.getLogin().getEncrypt();
        if (encrypt.getMode().equalsIgnoreCase(name()) && encrypt.getSecretKey() != null) {
            key = encrypt.getSecretKey();
        }
        sm4 = SmUtil.sm4(HexUtil.decodeHex(key));
    }

    /**
     * 加密
     *
     * @param plaintext
     * @return
     */
    @Override
    public String encrypt(String plaintext) throws SlardarException {
        try {
            return sm4.encryptHex(plaintext);
        } catch (Exception e) {
            throw new SlardarException(e);
        }
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
            return sm4.decryptStr(ciphertext, CharsetUtil.CHARSET_UTF_8);
        } catch (Exception e) {
            throw new SlardarException(e);
        }
    }
}