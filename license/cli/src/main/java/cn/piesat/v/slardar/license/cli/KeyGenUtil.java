package cn.piesat.v.slardar.license.cli;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/9/17
 */
public final class KeyGenUtil {

    private static final String ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    // 生成密钥对的方法
    public static void generateKeyPair(String publicKeyPath, String privateKeyPath) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
        keyGen.initialize(KEY_SIZE);
        KeyPair pair = keyGen.generateKeyPair();

        // 保存公钥
        Files.write(Paths.get(publicKeyPath), pair.getPublic().getEncoded());
        // 保存私钥
        Files.write(Paths.get(privateKeyPath), pair.getPrivate().getEncoded());
    }

    public static String sign(String data, String privateKeyPath) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(privateKeyPath));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
        PrivateKey privateKey = kf.generatePrivate(spec);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes());

        return Base64.getEncoder().encodeToString(signature.sign());
    }
}
