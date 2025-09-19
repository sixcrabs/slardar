package cn.piesat.v.slardar.license.cli;

import cn.piesat.v.slardar.license.core.DateTimeUtil;
import cn.piesat.v.slardar.license.core.KeyAlgorithm;
import cn.piesat.v.slardar.license.core.License;
import cn.piesat.v.slardar.license.core.LicenseFile;
import com.google.gson.Gson;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static cn.piesat.v.slardar.license.core.LicenseUtil.*;


/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/9/17
 */
@CommandLine.Command(
        mixinStandardHelpOptions = true,
        name = "java -jar slardar-license-cli-1.0.jar",
        version = "1.0",
        sortOptions = false,
        headerHeading = "Usage:",
        synopsisHeading = "%n",
        parameterListHeading = "%n参数:%n",
        optionListHeading = "%n选项:%n"
)
public class LicenseCommander implements Callable<Integer> {


    @CommandLine.Option(names = {"-c", "--customer"}, description = "客户信息, 生成许可时必须")
    Map<CustomerKey, String> customerInfo = new HashMap<>(1);

    @CommandLine.Option(names = {"-o", "--output"}, description = "输出lic文件名")
    String outputFile;

    @CommandLine.Option(names = {"-i", "--input"}, description = "License文件完整路径, 进行许可验证时必须")
    String inputFile;

    @CommandLine.Option(names = {"-v", "--verbose"}, description = "详细输出, 默认开启", negatable = true, defaultValue = "true", fallbackValue = "true")
    boolean verbose;

    @CommandLine.Parameters(index = "0", description = "要执行的操作，合法值: ${COMPLETION-CANDIDATES}", type = CliAction.class, defaultValue = "generate")
    CliAction action;

    private static final Gson GSON = new Gson();

    public static void main(String[] args) {
        int exitCode = new CommandLine(new LicenseCommander()).execute(args);
        System.exit(exitCode);
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Integer call() throws Exception {
        if (verbose) {
            System.out.println("Verbose mode ON");
        }
        switch (action) {
            case generate:
                handleGenerate();
                break;
            case verify:
                handleVerify();
                break;
            default:
                throw new Exception("未知操作: " + action);
        }
        return 0;
    }

    /**
     * 生成授权文件
     *
     * @throws Exception
     */
    private void handleGenerate() throws Exception {
        if (customerInfo.isEmpty()) {
            throw new Exception("生成授权文件必须提供客户信息");
        }
        String cname = customerInfo.get(CustomerKey.name);
        if (isBlank(outputFile)) {
            outputFile = cname + ".lic";
        }
        // 验证输入的过期时间是否有效 格式 yyyyMMdd
        String expiryDateStr = customerInfo.get(CustomerKey.expired);
        if (isBlank(expiryDateStr)) {
            throw new Exception("必须提供过期时间");
        }
        LocalDate expiryDate = DateTimeUtil.toLocalDate(expiryDateStr, "yyyyMMdd");
        if (expiryDate.isBefore(LocalDate.now())) {
            throw new Exception("提供的过期时间无效");
        }
        // 获取jar当前所在的目录作为输出目录
        String pwd;
        try {
            File jarFile = new File(LicenseCommander.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            pwd = jarFile.getParentFile().getAbsolutePath();
        } catch (Exception e) {
            System.err.println("无法获取 JAR 所在目录: " + e.getMessage());
            return;
        }
        if (verbose) {
            System.out.println("输出位置: " + pwd + File.separator + outputFile);
        }
        // FIXME: 私钥文件不和许可文件同目录？
        Path privateKeyPath = Paths.get(pwd, "private.key");
        // 1. 生成密钥对 私钥保存到文件，公钥返回编码后的字符串
        String base64Pub = generateKeyPair(KeyAlgorithm.ECC, privateKeyPath, verbose);
        License license = new License()
                .setId(uuid())
                .setCustomerName(cname)
                .setCustomerEmail(customerInfo.get(CustomerKey.email))
                .setCustomerContact(customerInfo.get(CustomerKey.contact))
                .setIssueDate(DateTimeUtil.format(LocalDate.now(), DateTimeUtil.DATE_PATTERN))
                .setExpiryDate(DateTimeUtil.format(expiryDate, DateTimeUtil.DATE_PATTERN))
                .setIssuedBy("slardar")
                .setProductCode(customerInfo.get(CustomerKey.productCode))
                .setMachineFingerprint(customerInfo.get(CustomerKey.machineCode));
        String licenseStr = GSON.toJson(license);
        // 2. 对 license 信息生成签名
        String signature = sign(KeyAlgorithm.ECC, privateKeyPath, licenseStr);
        LicenseFile licenseFile = new LicenseFile()
                .setPublicKey(base64Pub)
                .setLicense(base64Encode(licenseStr))
                .setSignature(signature);
        Path licFilePath = Paths.get(pwd, outputFile);
        Files.write(licFilePath, base64Encode(GSON.toJson(licenseFile)).getBytes());
        System.out.println("License 文件已生成: " + licFilePath + " 请妥善保管 private.key， 切勿分享或公开！");
    }

    /**
     * 验证授权文件
     * @throws Exception
     */
    private void handleVerify() throws Exception {
        if (isBlank(inputFile)) {
            throw new Exception("请提供 License 文件路径");
        }
        String licenseFileStr = new String(base64Decode(Files.readAllBytes(Paths.get(inputFile))));
        LicenseFile licenseFile = GSON.fromJson(licenseFileStr, LicenseFile.class);
        String licenseStr = base64Decode(licenseFile.getLicense());
        String signature = licenseFile.getSignature();
        String publicKey = licenseFile.getPublicKey();
        if (verbose) {
            System.out.println("License info: " + licenseStr);
        }
        License license = GSON.fromJson(licenseStr, License.class);
        boolean sigValid = verify(KeyAlgorithm.ECC, publicKey, licenseStr, signature);
        if (!sigValid) {
            System.err.println("License 签名无效，可能已经被篡改！");
            return;
        }
        // 验证时间是否过期
        boolean verified = DateTimeUtil.toLocalDate(license.getExpiryDate()).isAfter(LocalDate.now());
        System.out.println("License 文件验证结果: " + (verified ? "有效" : "无效"));
    }
}
