package cn.piesat.nj.slardar.starter.authenticate.crypto.impl;

import com.antherd.smcrypto.sm4.Sm4;
import com.antherd.smcrypto.sm4.Sm4Options;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/29
 */
class Sm4CryptoTest {

    /**
     * // 16 进制字符串，要求为 128 比特
     */
    private String key = "0a1b2c3d4e5f6f7e8d9cba9876543210";


    @Test
    void encrypt() {
        String plaintext = "zhangsan123";
        Sm4Options sm4Options2 = new Sm4Options();
        sm4Options2.setPadding("none");
        // 加密，不使用 padding，输出16进制字符串
        String encrypt = Sm4.encrypt(plaintext, key, sm4Options2);
        System.out.println(encrypt);
    }

    @Test
    void decrypt() {
        String ciphertext = "1888d09ab6cc672d0017ab63a9f96b55";
        // 解密，不使用 padding，输出 utf8 字符串
        Sm4Options sm4Options6 = new Sm4Options();
        sm4Options6.setPadding("none");
        String decrypt = Sm4.decrypt(ciphertext, key, sm4Options6);
        System.out.println(decrypt);
    }
}