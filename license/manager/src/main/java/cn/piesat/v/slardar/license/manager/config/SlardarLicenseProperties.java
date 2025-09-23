package cn.piesat.v.slardar.license.manager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * 许可授权相关的 配置项
 * </p>
 *
 * @author Alex
 * @since 2025/9/22
 */
@ConfigurationProperties(prefix = "slardar.license")
public class SlardarLicenseProperties {

    /**
     * 许可文件 全路径
     */
    private String licFile;

    /**
     * 产品编码或名称
     */
    private String productCode;

    public String getLicFile() {
        return licFile;
    }

    public SlardarLicenseProperties setLicFile(String licFile) {
        this.licFile = licFile;
        return this;
    }

    public String getProductCode() {
        return productCode;
    }

    public SlardarLicenseProperties setProductCode(String productCode) {
        this.productCode = productCode;
        return this;
    }
}
