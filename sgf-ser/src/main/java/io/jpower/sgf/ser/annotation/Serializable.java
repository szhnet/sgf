package io.jpower.sgf.ser.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记该类型可以进行序列化
 * <p>
 * <ul>
 * <li>类需要有无参构造方法</li>
 * </ul>
 *
 * @author zheng.sun
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Serializable {

}
