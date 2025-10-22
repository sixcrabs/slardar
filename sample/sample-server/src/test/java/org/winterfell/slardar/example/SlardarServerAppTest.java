package org.winterfell.slardar.example;

import org.winterfell.slardar.starter.support.Base64;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/4/17
 */
public class SlardarServerAppTest {

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @Test
    public void testBase64() {
        byte[] bytes = Base64.encodeBase64("zhangsan:zhangsan123".getBytes(StandardCharsets.UTF_8));
        System.out.println(new String(bytes, StandardCharsets.UTF_8));

    }
}