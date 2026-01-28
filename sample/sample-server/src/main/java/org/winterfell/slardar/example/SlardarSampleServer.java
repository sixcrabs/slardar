/*
 * @Author: alex
 * @Date: 2023-11-16 21:09:15
 * @LastEditTime: 2023-11-17 20:06:33
 * @LastEditors: alex
 */
package org.winterfell.slardar.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/14
 */
@SpringBootApplication(scanBasePackages = "org.winterfell")
public class SlardarSampleServer {

    public static void main(String[] args) {
        SpringApplication.run(SlardarSampleServer.class, args);
    }
}