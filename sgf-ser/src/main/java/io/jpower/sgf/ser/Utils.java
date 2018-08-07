package io.jpower.sgf.ser;

import static java.util.Locale.ENGLISH;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.jpower.sgf.utils.JavaUtils;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
class Utils {

    static final Charset CHARSET = StandardCharsets.UTF_8;

    static final Object[] EMPTY_OBJS = new Object[0];

    static final Class<?>[] EMPTY_CLASSES = new Class[0];

    static final byte[] EMPTY_BYTES = new byte[0];

    static final String GET_PREFIX = "get";

    static final String SET_PREFIX = "set";

    static final String IS_PREFIX = "is";

    static void checkFieldNumber(int fieldNumber) {
        if (fieldNumber <= 0) {
            throw new SerializationException("fieldNumber <= 0: " + fieldNumber);
        }
        if (fieldNumber > WireFormat.MAX_FIELD_NUMBER) {
            throw new SerializationException(
                    "fieldNumber > " + WireFormat.MAX_FIELD_NUMBER + ": " + fieldNumber);
        }
    }

    static String capitalize(String name) { // 来自java.beans.NameGenerator
        if (name == null || name.length() == 0) {
            return name;
        }
        return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
    }

    static Object invoke(Method method, Object obj, Object... params) {
        try {
            return method.invoke(obj, params);
        } catch (Exception e) {
            throw JavaUtils.sneakyThrow(e);
        }
    }

}
