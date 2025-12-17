package org.winterfell.slardar.ext.apisix;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/12/16
 */
@ConfigurationProperties(prefix = "slardar.apisix")
@Data
public class SlardarApisixProperties {

    private boolean enabled = true;

    private String verifyUrl = "/apisix/verify";
}