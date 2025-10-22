package cn.piesat.v.slardar.example.config;

import org.winterfell.slardar.starter.config.customizer.SlardarIgnoringCustomizer;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/4/11
 */
@Component
public class MyIgnoreRegistryImpl implements SlardarIgnoringCustomizer {

    /**
     * 自定义过滤需要忽略的url
     *
     * @param antPatterns
     */
    @Override
    public void customize(List<String> antPatterns) {
        antPatterns.add("/api/hello");
    }
}
