package cn.piesat.nj.slardar.starter.authenticate.crypto.impl;

import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.authenticate.crypto.SlardarCrypto;
import cn.piesat.nj.slardar.starter.config.SlardarProperties;
import com.antherd.smcrypto.sm4.Sm4;
import com.antherd.smcrypto.sm4.Sm4Options;
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
    private String key = "0123456789abcdeffedcba9876543210";

    /**
     * 加密模式
     * aes/sm3/sm4/...
     *
     * @return
     */
    @Override
    public String mode() {
        return "sm4";
    }

    /**
     * set context
     *
     * @param context
     */
    @Override
    public void setContext(SlardarContext context) {
        SlardarProperties properties = context.getBeanIfAvailable(SlardarProperties.class);
        SlardarProperties.EncryptSetting encrypt = properties.getLogin().getEncrypt();
        if (encrypt.getSecretKey() != null) {
            key = encrypt.getSecretKey();
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
        Sm4Options sm4Options2 = new Sm4Options();
        sm4Options2.setPadding("none");
        // 加密，不使用 padding，输出16进制字符串
        return Sm4.encrypt(plaintext, key, sm4Options2);
    }

    /**
     * 解密
     *
     * @param ciphertext
     * @return
     */
    @Override
    public String decrypt(String ciphertext) throws SlardarException {
        // 解密，不使用 padding，输出 utf8 字符串
        Sm4Options sm4Options6 = new Sm4Options();
        sm4Options6.setPadding("none");
        return Sm4.decrypt(ciphertext, key, sm4Options6);
    }
}
