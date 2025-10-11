package org.winterfell.slardar.sample.sso.client;

import org.winterfell.slardar.core.SlardarSecurityHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
@RestController("/api")
public class SsoClientController {

    @GetMapping("/demo")
    public String demo() {
        return "Hello, " + SlardarSecurityHelper.getUserProfile().getName() + " ----from SSO Client:".concat(String.valueOf(new Random(12345L).nextInt()));
    }

}
