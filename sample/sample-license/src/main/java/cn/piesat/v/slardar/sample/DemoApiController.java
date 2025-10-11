package cn.piesat.v.slardar.sample;

import cn.piesat.v.shared.as.response.Resp;
import org.winterfell.slardar.core.annotation.SlardarIgnore;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/9/26
 */
@RestController
@RequestMapping("/demo/api")
@Tag(name = "示例Api")
public class DemoApiController {

    @GetMapping("/hi")
    @SlardarIgnore
    public Resp sayHi() {
        return Resp.of("Hello, Slardar");
    }
}
