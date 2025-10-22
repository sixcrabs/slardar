package cn.piesat.v.slardar.example.web;

import org.springframework.web.bind.annotation.RequestParam;
import org.winterfell.misc.hutool.mini.MapUtil;
import org.winterfell.misc.hutool.mini.RandomUtil;
import org.winterfell.shared.as.advice.response.Response;
import org.winterfell.shared.as.advice.response.ResponseFactory;
import org.winterfell.slardar.core.SlardarSecurityHelper;
import org.winterfell.slardar.core.annotation.AuditLogger;
import org.winterfell.slardar.core.annotation.SlardarAuthority;
import org.winterfell.slardar.core.annotation.SlardarIgnore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/16
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    @Resource
    private ResponseFactory responseFactory;

    @GetMapping("/greeting")
    public Response<String> sayHi() {
        return responseFactory.createSuccess("Hello, ".concat(Objects.requireNonNull(SlardarSecurityHelper.getCurrentUsername())));
    }

    @GetMapping("/name")
    @AuditLogger(detail = "被忽略的方法")
    @SlardarIgnore
    public String getName() {
        return RandomUtil.randomString(18);
    }

    @GetMapping("/guessMe")
    @SlardarIgnore
    public String testExt(@RequestParam Integer money, @RequestParam(required = false) boolean bonus) {
        return (bonus ? money * 10 : money) > RandomUtil.randomInt(0, 100) ? "success" : "fail";
    }

    @GetMapping("/admin/demo")
    @SlardarAuthority("hasRole('ADMIN')")
    public Map<String, String> onlyAdmin() {
        return MapUtil.of("content", "admin_".concat(RandomUtil.randomString(12)));
    }
}
