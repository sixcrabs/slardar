package cn.piesat.v.slardar.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 注解用于 通过切面记录访问日志信息
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/6/21
 */
@Target({ElementType.METHOD}) // 支持方法上添加该注解
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLogger {

    /**
     * 类型 自定义值
     * @return
     */
    String type() default "";

    /**
     * 详细描述
     * @return
     */
    String detail() default "";
}
