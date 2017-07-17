package io.jpower.sgf.ser;

import io.jpower.sgf.enumtype.EnumUtils;
import io.jpower.sgf.enumtype.IntEnum;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
enum Ide implements IntEnum {

    IDEA(1),

    ECLIPSE(2),;

    private static final Ide[] INDEXES = EnumUtils.toArray(values());

    private final int id;

    Ide(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public static Ide findById(int id) {
        if (id < 0 || id >= INDEXES.length) {
            return null;
        }
        return INDEXES[id];
    }

}
