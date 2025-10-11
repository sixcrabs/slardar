package org.winterfell.slardar.license.core;

/**
 * <p>
 * 许可文件对象
 * - 许可信息（base64编码的序列化字符串）
 * - 公钥
 * - 许可信息的数字签名
 * </p>
 *
 * @author Alex
 * @since 2025/9/18
 */
public class LicenseFile {

    /**
     * 密钥生成的数字签名(base64编码)
     */
    private String signature;

    /**
     * 公钥 用于验证 (base64编码)
     */
    private String publicKey;

    /**
     * 许可信息（包含过期时间、客户信息） (base64编码)
     * @see License
     */
    private String license;


    public String getSignature() {
        return signature;
    }

    public LicenseFile setSignature(String signature) {
        this.signature = signature;
        return this;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public LicenseFile setPublicKey(String publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public String getLicense() {
        return license;
    }

    public LicenseFile setLicense(String license) {
        this.license = license;
        return this;
    }
}
