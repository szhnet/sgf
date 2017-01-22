package io.jpower.sgf.ser.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可以用来标记方法，此方法会在反序列化之前被调用。
 * <p>
 * <ul>
 * <li>需要方法是public，无参数的。</li>
 * </ul>
 *
 * @author zheng.sun
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface BeforeDeserialize {

}
