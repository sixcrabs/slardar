package org.winterfell.slardar.ext.ldap;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/24
 */
@EnableConfigurationProperties(LdapProperties.class)
@AutoConfiguration
public class LdapSlardarAutoConfiguration {

}