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
    private String licenseFile;

    /**
     * 产品编码或名称
     */
    private String productCode;

    /**
     * true 则每次启动都安装许可（覆盖）
     */
    private boolean installAlways = false;

    public String getLicenseFile() {
        return licenseFile;
    }

    public SlardarLicenseProperties setLicenseFile(String licenseFile) {
        this.licenseFile = licenseFile;
        return this;
    }

    public String getProductCode() {
        return productCode;
    }

    public SlardarLicenseProperties setProductCode(String productCode) {
        this.productCode = productCode;
        return this;
    }

    public boolean isInstallAlways() {
        return installAlways;
    }

    public void setInstallAlways(boolean installAlways) {
        this.installAlways = installAlways;
    }
}
