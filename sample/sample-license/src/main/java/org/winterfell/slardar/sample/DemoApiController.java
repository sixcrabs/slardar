package org.winterfell.slardar.sample;

import org.winterfell.shared.as.advice.response.Response;
import org.winterfell.shared.as.advice.response.ResponseFactory;
import org.winterfell.slardar.core.annotation.SlardarIgnore;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


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

    @Resource
    private ResponseFactory responseFactory;

    @GetMapping("/hi")
    @SlardarIgnore
    public Response<String> sayHi() {
        return responseFactory.createSuccess("Hello, Slardar");
    }
}