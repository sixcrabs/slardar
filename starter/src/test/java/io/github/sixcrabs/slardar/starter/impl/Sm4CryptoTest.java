package io.github.sixcrabs.slardar.starter.impl;

import io.github.sixcrabs.slardar.core.SlardarException;
import com.bastiaanjansen.otp.HMACAlgorithm;
import com.bastiaanjansen.otp.TOTPGenerator;
import io.github.sixcrabs.winterfell.mini.CharsetUtil;
import io.github.sixcrabs.winterfell.mini.HexUtil;
import io.github.sixcrabs.winterfell.mini.crypto.SmUtil;
import io.github.sixcrabs.winterfell.mini.crypto.symmetric.SM4;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

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

    public static final String text = "{\"1\":\"landtype='N0101'\",\"2\":\"landtype='N0101'\",\"3\":\"landtype='N0101'\",\"4\":\"landtype='N0101'\",\"5\":\"landtype='N0101'\",\"6\":\"landtype='N0101'\",\"7\":\"landtype='N0101'\",\"8\":\"landtype='N0101'\",\"9\":\"landtype='N0101'\"}";


    SM4 sm4 = SmUtil.sm4(HexUtil.decodeHex(key));

    @Test
    void encrypt() throws SlardarException {

        String content = "test中文";
//        SM4 sm4 = SmUtil.sm4();

        String encryptHex = sm4.encryptHex(content);
        String decryptStr = sm4.decryptStr(encryptHex, CharsetUtil.CHARSET_UTF_8);
        System.out.println(encryptHex);
        System.out.println(decryptStr);
    }

    @Test
    void decrypt() {
        String ciphertext = "1888d09ab6cc672d0017ab63a9f96b55";
        // 解密，不使用 padding，输出 utf8 字符串
//        Sm4Options sm4Options6 = new Sm4Options();
//        sm4Options6.setPadding("none");
//        String decrypt = Sm4.decrypt(ciphertext, key, sm4Options6);
//        System.out.println(decrypt);
    }

    TOTPGenerator totp;

    @BeforeEach
    void setUp() {
        // 生成密钥
        String secret = "AO5BGGUEV3LCFOKTLZXDO2MC4KQMMZM4";//
//        SecretGenerator.generate();
        totp = new TOTPGenerator.Builder(secret.getBytes())
                .withHOTPGenerator(builder -> {
                    // 一次性密码的长度
                    builder.withPasswordLength(6);
                    // 散列算法
                    builder.withAlgorithm(HMACAlgorithm.SHA512);
                })
                // 时间周期
                .withPeriod(Duration.ofSeconds(20))
                .build();
    }

    @Test
    void test2() {
        System.out.println("验证code：" + totp.verify("537761"));
    }

    @Test
    void testTOTP() throws InterruptedException {

        // 生成一次性密码
        String code = totp.now();
        System.out.println("totp生成的一次性密码：" + code);
        Thread.sleep(5000);
        String code1 = totp.now();
        System.out.println("code1:" + code1);

//        TOTPGenerator nTotp = new TOTPGenerator.Builder(secret)
//                .withHOTPGenerator(builder -> {
//                    // 一次性密码的长度
//                    builder.withPasswordLength(6);
//                    // 散列算法
//                    builder.withAlgorithm(HMACAlgorithm.SHA512);
//                })
//                // 时间周期
//                .withPeriod(Duration.ofSeconds(30))
//                .build();
        boolean verify = totp.verify(code);
        System.out.println("totp验证一次性密码结果：" + verify);

        System.out.println("验证code1：" + totp.verify(code1));

    }
}