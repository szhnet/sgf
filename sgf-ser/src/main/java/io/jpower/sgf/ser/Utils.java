package io.jpower.sgf.ser;

import static java.util.Locale.ENGLISH;

import java.lang.reflect.Method;

import io.jpower.sgf.utils.JavaUtils;

/**
 * @author zheng.sun
 */
class Utils {

    public static final Object[] EMPTY_OBJS = new Object[0];

    public static final Class<?>[] EMPTY_CLASSES = new Class[0];

    public static final byte[] EMPTY_BYTES = new byte[0];

    public static final String GET_PREFIX = "get";

    public static final String SET_PREFIX = "set";

    public static final String IS_PREFIX = "is";

    public static void checkFieldNumber(int fieldNumber) {
        if (fieldNumber <= 0) {
            throw new SerializationException("fieldNumber <= 0: " + fieldNumber);
        }
        if (fieldNumber > WireFormat.MAX_FIELD_NUMBER) {
            throw new SerializationException(
                    "fieldNumber > " + WireFormat.MAX_FIELD_NUMBER + ": " + fieldNumber);
        }
    }

    public static String capitalize(String name) { // 来自java.beans.NameGenerator
        if (name == null || name.length() == 0) {
            return name;
        }
        return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
    }

    public static Object invoke(Method method, Object obj, Object... params) {
        try {
            return method.invoke(obj, params);
        } catch (Exception e) {
            throw JavaUtils.sneakyThrow(e);
        }
    }

}
