package io.jpower.sgf.collection;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
class Helper {

    public static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    public static int intHash(int v) {
        return v;
    }

    public static int longHash(long v) {
        return (int) (v ^ (v >>> 32));
    }

    public static int floatHash(float v) {
        return Float.floatToIntBits(v);
    }

    public static int doubleHash(double v) {
        long bits = Double.doubleToLongBits(v);
        return (int) (bits ^ (bits >>> 32));
    }

    /**
     * Utility method for SimpleEntry and SimpleImmutableEntry. Test for
     * equality, checking for nulls.
     */
    public static boolean eq(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

}
