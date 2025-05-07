package cn.piesat.v.slardar.example.web;

import cn.hutool.core.util.RandomUtil;
import cn.piesat.v.shared.as.response.Resp;
import cn.piesat.v.slardar.core.SlardarSecurityHelper;
import cn.piesat.v.slardar.core.annotation.AuditLogger;
import cn.piesat.v.slardar.core.annotation.SlardarAuthority;
import cn.piesat.v.slardar.core.annotation.SlardarIgnore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


    @GetMapping("/greeting")
    public Resp sayHi() {
        return Resp.of("Hello, ".concat(Objects.requireNonNull(SlardarSecurityHelper.getCurrentUsername())));
    }

    @GetMapping("/name")
//    @AuditLogger(detail = "被忽略的方法")
//    @SlardarIgnore
    public Resp getName() {
        return Resp.of(RandomUtil.randomString(18));
    }

    @GetMapping("/admin/demo")
    @SlardarAuthority("hasRole('ADMIN')")
    public Resp onlyAdmin() {
        return Resp.of("admin_".concat(RandomUtil.randomString(12)));
    }
}
