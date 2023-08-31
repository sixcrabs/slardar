package cn.piesat.nj.slardar.sample.sso.client;

import cn.piesat.nj.slardar.core.SlardarSecurityHelper;
import cn.piesat.v.shared.as.response.Resp;
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
    public Resp demo() {
        return Resp.of("Hello, " + SlardarSecurityHelper.getUserProfile().getName() + " ----from SSO Client:".concat(String.valueOf(new Random(12345L).nextInt())));
    }

}
