package cn.piesat.v.slardar.starter.support.spi.crypto;

import cn.piesat.v.misc.hutool.mini.CharsetUtil;
import cn.piesat.v.misc.hutool.mini.HexUtil;
import cn.piesat.v.misc.hutool.mini.crypto.SmUtil;
import cn.piesat.v.misc.hutool.mini.crypto.symmetric.SM4;
import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.spi.crypto.SlardarCrypto;
import cn.piesat.v.slardar.starter.config.SlardarProperties;
import com.google.auto.service.AutoService;

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

    /**
     * // 16 进制字符串，要求为 128 比特
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
        return "sm4";
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
        return sm4.encryptHex(plaintext);
    }

    /**
     * 解密
     *
     * @param ciphertext
     * @return
     */
    @Override
    public String decrypt(String ciphertext) throws SlardarException {
        return sm4.decryptStr(ciphertext, CharsetUtil.CHARSET_UTF_8);
    }
}
