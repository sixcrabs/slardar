package cn.piesat.v.slardar.license.cli;

import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.Callable;

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
        headerHeading = "Usage:",
        synopsisHeading = "%n",
        parameterListHeading = "%n参数:%n",
        optionListHeading = "%n选项:%n"
)
public class LicenseGenerator implements Callable<Integer> {




    @CommandLine.Option(names = {"-u", "--user"}, description = "用户名, 必须", required = true)
    String user;

    @CommandLine.Option(names = {"-p", "--password"}, description = "Passphrase",
            interactive = true, prompt = "请输入密码:", echo = true)
    char[] password;

    @CommandLine.Option(names = {"-v", "--verbose"}, description = "详细输出, 默认开启", negatable = true, defaultValue = "true", fallbackValue = "true")
    boolean verbose;

    @CommandLine.Option(names = {"-o", "--output"}, description = "输出文件路径, 默认为当前目录")
    String outputPath;

    @CommandLine.Parameters(index = "0", description = "要执行的操作，合法值: ${COMPLETION-CANDIDATES}", type = CliAction.class, defaultValue = "generate")
    CliAction action;

    @CommandLine.Parameters(index = "1", description = "License文件路径, 进行许可验证时必须")
    String licFilePath;



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
        if (password.length == 0) {
            System.out.println("请输入密码");
            return -1;
        }
        byte[] bytes = new byte[password.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) password[i];
        }

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md.update(bytes);

        // null out the arrays when done
        Arrays.fill(bytes, (byte) 0);
        Arrays.fill(password, ' ');
        System.out.println("Hello, " + user + "! Action: " + action + " Pwd: " + new String(md.digest()));
        return 0;
    }
}
