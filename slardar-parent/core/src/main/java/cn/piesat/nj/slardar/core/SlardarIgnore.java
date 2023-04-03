package cn.piesat.nj.slardar.core;

import java.lang.annotation.*;

/**
 * <p>
 * 注解controller方法 标记忽略认证授权
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/21
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SlardarIgnore {

}
