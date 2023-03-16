package cn.piesat.nj.slardar.example.web;

import cn.hutool.core.util.RandomUtil;
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
}
