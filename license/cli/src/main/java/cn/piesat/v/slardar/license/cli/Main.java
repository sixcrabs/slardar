package cn.piesat.v.slardar.license.cli;

import picocli.CommandLine;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/9/17
 */
public class Main {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new LicenseGenerator()).execute(args);
        System.exit(exitCode);
    }


}