package cn.piesat.nj.slardar.starter.support;

import java.lang.annotation.*;

/**
 * <p>
 * 注解controller方法 标记 可访问权限
 * <code>
 *
 * </code>
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/21
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SlardarAuthority {

    /**
     * 定义可访问权限
     * - hasAnyRole('ADMIN')
     * - hasRole('xx')
     * - hasAuthority('xxx')
     * - hasAnyAuthority('xx','yy')
     * - permitAll()
     * - denyAll()
     * - ...
     * @return
     */
    String value();

}
