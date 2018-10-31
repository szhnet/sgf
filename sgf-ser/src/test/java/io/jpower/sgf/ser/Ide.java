package io.jpower.sgf.ser;

import io.jpower.sgf.enumtype.EnumUtils;
import io.jpower.sgf.enumtype.TagEnum;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public enum Ide implements TagEnum {

    IDEA(1),

    ECLIPSE(2),;

    private static final Ide[] INDEXES = EnumUtils.toArray(values());

    private final int id;

    Ide(int id) {
        this.id = id;
    }

    @Override
    public int tag() {
        return id;
    }

    public static Ide forTag(int tag) {
        if (tag < 0 || tag >= INDEXES.length) {
            return null;
        }
        return INDEXES[tag];
    }

}
