package io.jpower.sgf.ser.test.model;

import io.jpower.sgf.enumtype.EnumUtils;
import io.jpower.sgf.enumtype.IntEnum;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public enum TestCurrency implements IntEnum {

    FOOD(1),

    WOOD(2),

    IRON(3),

    SILVER(4),

    GOLD(5),

    DIAMOND(6),

    HONOUR(7),;

    private static final TestCurrency[] VALUES = values();

    private static final TestCurrency[] INDEXES = EnumUtils.toArray(VALUES);

    private final int id;


    private TestCurrency(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public static TestCurrency findById(int id) {
        if (id < 0 || id >= INDEXES.length) {
            return null;
        }
        return INDEXES[id];
    }

    public static TestCurrency[] fastValues() {
        return VALUES;
    }

}
