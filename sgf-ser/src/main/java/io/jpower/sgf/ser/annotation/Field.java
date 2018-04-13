package io.jpower.sgf.ser.annotation;

import io.jpower.sgf.ser.IntEncodeType;

import java.lang.annotation.*;

/**
 * 标记可序列化的类中的一个属性
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Field {

    /**
     * field number 字段的序号
     * <p>
     * <ul>
     * <li>值必须大于0，最大值为2^28 - 1</li>
     * <li>这个值被使用后，不能随意修改。序列化和反序列化需要通过此值来对应类中的属性。</li>
     * <li>一个类中的字段序号是不能重复的。</li>
     * </ul>
     *
     * @return
     */
    int value();

    /**
     * 用来表示整数的编码方式
     *
     * @return
     */
    IntEncodeType intEncodeType() default IntEncodeType.DEFAULT;

    /**
     * 用来标记字段需要调用相应的intern方法，当前只能用在String字段上。
     *
     * @return
     */
    boolean intern() default false;

    /**
     * 反序列化时，指定具体类型
     * <p>
     * 比如目标字段是个list，可以指定反序列化时使用的具体list实现。
     *
     * @return
     */
    Class<?> deSerClazz() default void.class;

}
