package cn.piesat.nj.slardar.example.event.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/1/3
 */
@Target({ElementType.TYPE, ElementType.METHOD}) // 支持类、方法上添加该注解
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessLog {
    String value() default ""; // 设置默认值为空字符串
}
