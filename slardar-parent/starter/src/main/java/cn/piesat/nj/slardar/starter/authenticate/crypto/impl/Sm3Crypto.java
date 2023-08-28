package cn.piesat.nj.slardar.starter.authenticate.crypto.impl;

import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.authenticate.crypto.SlardarCrypto;
import com.antherd.smcrypto.sm3.Sm3;
import com.google.auto.service.AutoService;

/**
 * <p>
 * .国密3 加解密
 * 只能杂凑 所以不能解密
 * 加密后比较
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/28
 */
@AutoService(SlardarCrypto.class)
public class Sm3Crypto implements SlardarCrypto {

    /**
     * 加密模式
     * aes/sm3/sm4/...
     *
     * @return
     */
    @Override
    public String mode() {
        return "sm3";
    }

    /**
     * set context
     *
     * @param context
     */
    @Override
    public void setContext(SlardarContext context) {

    }

    /**
     * 加密
     *
     * @param plaintext
     * @return
     */
    @Override
    public String encrypt(String plaintext) throws SlardarException {
        return Sm3.sm3(plaintext);
    }

    /**
     * 解密
     *
     * @param ciphertext
     * @return
     */
    @Override
    public String decrypt(String ciphertext) throws SlardarException {
        throw new SlardarException("不支持解密!");
    }
}
