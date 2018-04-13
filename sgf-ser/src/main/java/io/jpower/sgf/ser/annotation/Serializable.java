package io.jpower.sgf.ser.annotation;

import java.lang.annotation.*;

/**
 * 标记该类型可以进行序列化
 * <p>
 * <ul>
 * <li>类需要有无参构造方法</li>
 * </ul>
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Serializable {

}
