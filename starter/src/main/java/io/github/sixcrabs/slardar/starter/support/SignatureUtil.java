package io.github.sixcrabs.slardar.starter.support;

import io.github.sixcrabs.slardar.core.SlardarException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * <p>
 * 签名加密 util
 * </p>
 *
 * @author alex
 * @version v1.0 2022/11/1
 */
public final class SignatureUtil {

    private SignatureUtil() {
    }

    private static final String HMAC_SHA1 = "HmacSHA1";

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * HMAC_SHA256 加密
     *
     * @param data 数据
     * @param key  秘钥
     * @return 密文
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static String encryptHmacSha256(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secret = new SecretKeySpec(key.getBytes(), HMAC_SHA256);
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(secret);
        return toHexString(mac.doFinal(data.getBytes()));
    }

    /**
     * 生成签名
     * <p>
     * <p>
     * 签名算法说明：
     * 签名字符串 = URL的Path部分+ X-AppKey + X-Nonce + X-RequestTime
     * 签名值（即 X-Signature）= HmacSha256(ToUpper(签名字符串), SecretKey)
     * <br/>
     * 举例：
     * URL为：https://nync.piesat.cn/v1/common/getRegions
     * AppKey: 892113432
     * SecretKey: FB88B679D7754D24B527B6744A2D0D77
     * HEADER为：
     * X-Nonce: 730D7BBAF1914E8BA60E9C922D58A8C8
     * X-RequestTime: 2021-04-27 12:36:20
     * <p>
     * 则签名串为：
     * <pre>/v1/common/getRegions892113432730D7BBAF1914E8BA60E9C922D58A8C82021-04-27 12:36:205667A9DD9990497AA9E6D655FAC573B5</pre>
     * <p>
     * 转为大写后为：
     * <pre>/V1/COMMON/GETREGIONS892113432730D7BBAF1914E8BA60E9C922D58A8C82021-04-27 12:36:205667A9DD9990497AA9E6D655FAC573B5</pre>
     * </p>
     * 使用SecretKey进行HmacSha1计算，得到签名值
     *
     * </p>
     *
     * @param appKey       app key
     * @param secretKey    secret key
     * @param nonce       请求时需传入的唯一请求消息编码，一般为32位UUID 随机数
     * @param requestTime 请求时间，格式为：yyyy-MM-dd HH:mm:ss 用于禁止非法请求
     * @return
     */
    public static String generateSignature(String urlPath, String appKey, String secretKey, String nonce, String requestTime) throws SlardarException {
        String str = urlPath.concat(appKey).concat(nonce).concat(requestTime);
        try {
            return encryptHmacSha256(str.toUpperCase(), secretKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new SlardarException("HMAC-SHA256 加密失败 {}", e.getLocalizedMessage());
        }
    }

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}