package io.jpower.sgf.enumtype;

import java.lang.annotation.*;

/**
 * A field annotation to configure the enum number.
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Tag {

    int value();

}
