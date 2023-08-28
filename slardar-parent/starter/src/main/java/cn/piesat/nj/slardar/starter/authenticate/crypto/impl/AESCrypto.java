package cn.piesat.nj.slardar.starter.authenticate.crypto.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.authenticate.crypto.SlardarCrypto;
import cn.piesat.nj.slardar.starter.config.SlardarProperties;
import com.google.auto.service.AutoService;

import java.nio.charset.StandardCharsets;

/**
 * <p>
 * AES 加解密
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/28
 */
@AutoService(SlardarCrypto.class)
public class AESCrypto implements SlardarCrypto {

    public static final String MODE = "AES";

    private AES aes = new AES();

    /**
     * 加密模式
     * aes/sm3/sm4/...
     *
     * @return
     */
    @Override
    public String mode() {
        return MODE;
    }

    /**
     * set context
     *
     * @param context
     */
    @Override
    public void setContext(SlardarContext context) {

        SlardarProperties properties = context.getBeanIfAvailable(SlardarProperties.class);
        String secretKey = properties.getLogin().getEncrypt().getSecretKey();
        if (StrUtil.isBlank(secretKey)) {
            secretKey = "ab0c1de2fg3hi4jk5lmnopqrstuvwxyz";
        }
        aes = new AES(Mode.ECB, Padding.ISO10126Padding, secretKey.getBytes());
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
        return aes.decryptStrFromBase64(ciphertext, StandardCharsets.UTF_8);
    }
}
