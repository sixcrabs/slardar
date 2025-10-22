package org.winterfell.slardar.starter.config.customizer;

import java.util.List;

/**
 * <p>
 * 定制 url 忽略
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/22
 */
@FunctionalInterface
public interface SlardarIgnoringCustomizer {

    /**
     * 自定义过滤需要忽略的url
     * @param antPatterns
     */
    void customize(List<String> antPatterns);
}
