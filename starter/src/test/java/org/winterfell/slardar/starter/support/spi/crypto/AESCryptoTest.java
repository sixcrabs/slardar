package org.winterfell.slardar.starter.support.spi.crypto;

import cn.piesat.v.misc.hutool.mini.crypto.CipherMode;
import cn.piesat.v.misc.hutool.mini.crypto.Mode;
import cn.piesat.v.misc.hutool.mini.crypto.Padding;
import cn.piesat.v.misc.hutool.mini.crypto.SecureUtil;
import cn.piesat.v.misc.hutool.mini.crypto.symmetric.AES;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/4/16
 */
public class AESCryptoTest {



    @Test
    void testEncrypt() {
        AES aes = new AES(Mode.ECB, Padding.ISO10126Padding, "12345abcdef67890".getBytes());
        String string = aes.encryptBase64("S0ystem*2021");
        System.out.println(string);
        String str = aes.decryptStr(string);
        System.out.println(str);
    }

    @Test
    void testDecrypt() {
        AES aes = new AES(Mode.ECB, Padding.ISO10126Padding, "12345abcdef67890".getBytes());
        String val = aes.decryptStr("tsDfXFJjfgjinkqsube7OQ==");
        System.out.println(val);
    }

    @Test
    void test2() {
        String val = SecureUtil.aes("12345abcdef67890".getBytes()).setMode(CipherMode.decrypt).decryptStr("U2FsdGVkX19AiqZ67VUbShDCDJUgHt0ivg3skGTKcHM=");
        System.out.println(val);
    }
}