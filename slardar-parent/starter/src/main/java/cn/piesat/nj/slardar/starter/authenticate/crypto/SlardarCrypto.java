package cn.piesat.nj.slardar.starter.authenticate.crypto;

import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.starter.SlardarContext;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/28
 */
public interface SlardarCrypto {


    /**
     * 加密模式
     * aes/sm3/sm4/...
     *
     * @return
     */
    String mode();


    /**
     * set context
     * @param context
     */
    void setContext(SlardarContext context);

    /**
     * 加密
     *
     * @param plaintext
     * @return
     */
    String encrypt(String plaintext) throws SlardarException;


    /**
     * 解密
     *
     * @param ciphertext
     * @return
     */
    String decrypt(String ciphertext) throws SlardarException;


}
