package org.winterfell.slardar.license.core;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/9/18
 */
public final class LicenseUtil {

    private LicenseUtil() {
    }


    private static final int KEY_SIZE = 2048;

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 生成密钥对
     * - 保存私钥到文件
     *
     * @param algorithm
     * @param privateKeyPath
     * @return base64 编码后的公钥
     * @throws Exception
     */
    public static String generateKeyPair(KeyAlgorithm algorithm, Path privateKeyPath, boolean verbose) throws Exception {
        KeyPairGenerator keyGen = algorithm.equals(KeyAlgorithm.RSA) ? KeyPairGenerator.getInstance("RSA") :
                KeyPairGenerator.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
        if (KeyAlgorithm.RSA.equals(algorithm)) {
            keyGen.initialize(KEY_SIZE);
        } else {
            keyGen.initialize(ECNamedCurveTable.getParameterSpec("secp256r1"), new SecureRandom());
        }
        KeyPair kp = keyGen.generateKeyPair();
        // 发给对方
        String pubStr = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
        // 自己保密
        String prvStr = Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded());
        if (verbose) {
            System.out.printf("公钥: %s\n", pubStr);
            System.out.printf("私钥: %s\n", prvStr);
        }
        Files.write(privateKeyPath, prvStr.getBytes());
        return pubStr;
    }


    /**
     * 用私钥对数据进行加密签名后 base64 编码返回
     *
     * @param algorithm
     * @param privateKeyPath
     * @param data
     * @return base64 编码后的签名
     * @throws Exception
     */
    public static String sign(KeyAlgorithm algorithm, Path privateKeyPath, String data) throws Exception {
        byte[] keyBytes = Files.readAllBytes(privateKeyPath);
        PrivateKey privateKey = loadPrivateKey(algorithm, new String(keyBytes));
        Signature signature = algorithm.equals(KeyAlgorithm.RSA) ? Signature.getInstance("SHA256withRSA") :
                Signature.getInstance("SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME);
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signed = signature.sign();
        return Base64.getEncoder().encodeToString(signed);
    }

    /**
     * 验签
     *
     * @param base64Pub  Base64公钥
     * @param data       原始消息
     * @param base64Sign Base64签名
     * @return true=校验通过
     */
    public static boolean verify(KeyAlgorithm algorithm, String base64Pub, String data, String base64Sign) throws GeneralSecurityException {
        PublicKey pub = loadPublicKey(algorithm, base64Pub);
        Signature sig = algorithm.equals(KeyAlgorithm.ECC) ? Signature.getInstance("SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME) :
                Signature.getInstance("SHA256withRSA");
        sig.initVerify(pub);
        sig.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signBytes = Base64.getDecoder().decode(base64Sign);
        return sig.verify(signBytes);
    }

    /**
     * 加载公钥
     *
     * @param algorithm
     * @param base64
     * @return
     * @throws GeneralSecurityException
     */
    private static PublicKey loadPublicKey(KeyAlgorithm algorithm, String base64) throws GeneralSecurityException {
        byte[] der = Base64.getDecoder().decode(base64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
        KeyFactory kf = algorithm.equals(KeyAlgorithm.ECC) ? KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME) :
                KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    /**
     * 加载私钥
     *
     * @param algorithm
     * @param base64
     * @return
     * @throws GeneralSecurityException
     */
    private static PrivateKey loadPrivateKey(KeyAlgorithm algorithm, String base64) throws GeneralSecurityException {
        byte[] der = Base64.getDecoder().decode(base64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        KeyFactory kf = algorithm.equals(KeyAlgorithm.ECC) ? KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME) :
                KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    /**
     * base64 编码
     *
     * @param text
     * @return
     */
    public static String base64Encode(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes());
    }

    /**
     * base64 解码
     *
     * @param encodedText
     * @return
     */
    public static String base64Decode(String encodedText) {
        return new String(Base64.getDecoder().decode(encodedText));
    }

    public static byte[] base64Decode(byte[] encoded) {
        return Base64.getDecoder().decode(encoded);
    }

    /**
     * 生成一个 uuid 字符串
     *
     * @return
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 判断字符串是否为空
     *
     * @param text
     * @return
     */
    public static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

}
