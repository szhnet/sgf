package io.jpower.sgf.ser.annotation;

import java.lang.annotation.*;

/**
 * 可以用来标记方法，此方法会在序列化之后被调用。
 * <p>
 * <ul>
 * <li>需要方法是public，无参数的。</li>
 * </ul>
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AfterSerialize {

}
