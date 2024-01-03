package cn.piesat.nj.slardar.example.web;

import cn.hutool.core.util.RandomUtil;
import cn.piesat.nj.slardar.core.SlardarAuthority;
import cn.piesat.nj.slardar.core.SlardarIgnore;
import cn.piesat.nj.slardar.example.event.aop.AccessLog;
import cn.piesat.v.shared.as.response.Resp;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


    @GetMapping("/hi")
    public Resp sayHi() {
        return Resp.of("Hello, ".concat(RandomUtil.randomString(6)));
    }

    @GetMapping("/name")
    @SlardarIgnore
    @AccessLog("匿名方法")
    public Resp getName() {
        return Resp.of(RandomUtil.randomString(18));
    }

    @GetMapping("/admin/demo")
    public Resp onlyAdmin() {
        return Resp.of("admin_".concat(RandomUtil.randomString(12)));
    }
}
