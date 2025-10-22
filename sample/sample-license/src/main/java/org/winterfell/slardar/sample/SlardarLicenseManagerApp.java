package org.winterfell.slardar.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/9/22
 */
@SpringBootApplication(scanBasePackages = {"cn.piesat", "org.winterfell"})
public class SlardarLicenseManagerApp {
    public static void main(String[] args) {
        SpringApplication.run(SlardarLicenseManagerApp.class, args);
    }
}