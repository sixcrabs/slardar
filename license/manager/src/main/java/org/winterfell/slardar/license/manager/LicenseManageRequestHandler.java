package org.winterfell.slardar.license.manager;

import cn.piesat.v.misc.hutool.mini.MapUtil;
import cn.piesat.v.misc.hutool.mini.StringUtil;
import org.winterfell.slardar.license.core.DateTimeUtil;
import org.winterfell.slardar.license.core.KeyAlgorithm;
import org.winterfell.slardar.license.core.License;
import org.winterfell.slardar.license.core.LicenseFile;
import org.winterfell.slardar.license.manager.config.SlardarLicenseProperties;
import org.winterfell.slardar.spi.SlardarKeyStore;
import org.winterfell.slardar.spi.SlardarSpiFactory;
import org.winterfell.slardar.starter.config.SlardarProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.Map;

import static org.winterfell.slardar.license.core.LicenseUtil.base64Decode;
import static org.winterfell.slardar.license.core.LicenseUtil.verify;
import static org.winterfell.slardar.license.manager.support.LicenseManagerUtil.*;
import static org.winterfell.slardar.starter.support.SecUtil.GSON;

/**
 * <p>
 * 处理 license/* 请求
 * </p>
 *
 * @author Alex
 * @since 2025/9/23
 */
public class LicenseManageRequestHandler implements InitializingBean {

    public static final Logger logger = LoggerFactory.getLogger(LicenseManageRequestHandler.class);

    private final SlardarLicenseProperties licenseProperties;

    private final SlardarKeyStore keyStore;

    public static final String LICENSE_KEY_PREFIX = "license_";

    public LicenseManageRequestHandler(SlardarLicenseProperties licenseProperties, SlardarProperties slardarProperties,
                                       SlardarSpiFactory spiFactory) {
        this.licenseProperties = licenseProperties;
        this.keyStore = spiFactory.findKeyStore(slardarProperties.getKeyStore().getType());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void handle(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        String mapping = uri.replace("/license", "").replaceFirst("/", "");
        LicenseRequestMapping requestMapping = LicenseRequestMapping.valueOf(mapping);
        String productCode = request.getParameter("product");
        if (StringUtil.isBlank(productCode)) {
            productCode = licenseProperties.getProductCode();
        }
        try {
            switch (requestMapping) {
                case status:
                    if (!keyStore.has(LICENSE_KEY_PREFIX + productCode)) {
                        makeErrorResult("未找到许可信息", 4003);
                    }
                    String str = keyStore.get(LICENSE_KEY_PREFIX + productCode);
                    Map data = GSON.fromJson(str, Map.class);
                    boolean verified = DateTimeUtil.toLocalDate(MapUtil.getStr(data, "expiryDate")).isAfter(LocalDate.now());
                    data.put("status", verified ? "正常有效" : "已过期");
                    data.put("licFile", licenseProperties.getLicenseFile());
                    sendJson(response, HttpStatus.OK, makeResult(data, 200));
                    break;
                case install:
                    install(productCode);
                    break;
                case uninstall:
                    // TODO:
                    break;
                default:
                    throw new Exception("未知请求");
            }
        } catch (Exception e) {
            sendJson(response, HttpStatus.INTERNAL_SERVER_ERROR, makeErrorResult(e.getLocalizedMessage(), 4003));
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 检测store中是否有许可信息 如果没有 则立即install
        String productCode = licenseProperties.getProductCode();
        boolean existed = keyStore.has(LICENSE_KEY_PREFIX + productCode);
        if (existed && licenseProperties.isInstallAlways()) {
            logger.info("许可已安装，无需安装");
        } else {
            logger.info("正在安装许可, 若已存在 会覆盖更新！");
            install(productCode);
        }
    }

    public boolean verifyLicense() throws LicenseException {
        String productCode = licenseProperties.getProductCode();
        String licenseStr = keyStore.get(LICENSE_KEY_PREFIX + productCode);
        if (StringUtil.isBlank(licenseStr)) {
            return false;
        }
        License license = GSON.fromJson(licenseStr, License.class);
        if (!license.getProductCode().equals(productCode)) {
            logger.error("许可与当前产品[{}]不匹配！", productCode);
            throw new LicenseException("License 文件与当前产品不匹配", 4003);
        }
        // 验证时间是否过期
        boolean verified = DateTimeUtil.toLocalDate(license.getExpiryDate()).isAfter(LocalDate.now());
        if (!verified) {
            logger.error("授权文件已过期！");
            throw new LicenseException("授权文件已过期", 4005);
        }
        return true;
    }


    /**
     * 安装许可
     *
     * @param productCode
     * @throws LicenseException
     * @throws IOException
     * @throws GeneralSecurityException
     */
    private void install(String productCode) throws LicenseException, IOException, GeneralSecurityException {
        if (StringUtil.isBlank(productCode)) {
            logger.error("许可安装失败，请设置产品码 productCode");
            return;
        }
        String licFile = licenseProperties.getLicenseFile();
        // 文件是否存在
        if (!new File(licFile).exists()) {
            logger.error("授权文件[{}]不存在", licFile);
            throw new LicenseException("授权文件不存在");
        }
        String licenseData = new String(base64Decode(Files.readAllBytes(Paths.get(licFile))));
        LicenseFile licenseFile = GSON.fromJson(licenseData, LicenseFile.class);
        String licenseStr = base64Decode(licenseFile.getLicense());
        String signature = licenseFile.getSignature();
        String publicKey = licenseFile.getPublicKey();
        License license = GSON.fromJson(licenseStr, License.class);
        if (!license.getProductCode().equals(productCode)) {
            logger.error("授权文件[{}]与当前产品[{}]不匹配！", licFile, productCode);
            throw new LicenseException("授权文件与当前产品不匹配");
        }
        boolean sigValid = verify(KeyAlgorithm.ECC, publicKey, licenseStr, signature);
        if (!sigValid) {
            logger.error("授权文件签名无效，可能已经被篡改！");
            throw new LicenseException("授权文件签名无效，可能已经被篡改！");
        }
        keyStore.set(LICENSE_KEY_PREFIX + productCode, licenseStr);
        // 验证时间是否过期
        boolean verified = DateTimeUtil.toLocalDate(license.getExpiryDate()).isAfter(LocalDate.now());
        if (!verified) {
            logger.error("安装失败，授权[{}]已过期！", licFile);
            throw new LicenseException("安装授权失败，授权文件已过期");
        }
        logger.info("授权安装成功！有效期至[{}]", license.getExpiryDate());
    }

}
