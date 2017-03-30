package io.jpower.sgf.enumtype;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import io.jpower.sgf.collection.IntHashMap;
import io.jpower.sgf.collection.IntMap;
import io.jpower.sgf.utils.JavaUtils;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class EnumUtils {

    private static final int MAX_INDEX = 9999;

    /**
     * 将实现{@link IntEnum}接口的枚举转成数组，数组下标取{@link IntEnum#getId()}的值。
     *
     * @param enums
     * @return
     */
    public static <E extends IntEnum> E[] toArray(E[] enums) {
        int maxIndex = 0;
        for (E e : enums) {
            int curIdx = e.getId();

            if (curIdx < 0) {
                throw new IndexOutOfBoundsException("Enum index cannot be negative: Type="
                        + e.getClass() + ", index=" + curIdx);
            }

            if (curIdx > MAX_INDEX) {
                throw new IllegalStateException(
                        "Enum index is too big: Type=" + e.getClass() + ", index=" + curIdx);
            }

            if (curIdx > maxIndex) {
                maxIndex = curIdx;
            }

        }

        @SuppressWarnings("unchecked")
        E[] enumArray = (E[]) Array.newInstance(enums.getClass().getComponentType(), maxIndex + 1);
        for (E e : enums) {
            int curIdx = e.getId();

            E oldenum = enumArray[curIdx];
            if (oldenum != null) {
                throw new IllegalStateException(
                        "Enum has duplicate index: Type=" + e.getClass() + ", index=" + curIdx);
            }
            enumArray[curIdx] = e;
        }
        return enumArray;
    }

    /**
     * 将实现{@link IntEnum}接口的枚举转成一个Map(key:{@link IntEnum#getId()}, value:枚举对象)
     *
     * @param enums
     * @return
     */
    public static <E extends IntEnum> IntMap<E> toMap(E[] enums) {
        IntMap<E> map = new IntHashMap<E>();

        for (E e : enums) {
            int curIdx = e.getId();

            if (map.containsKey(curIdx)) {
                throw new IllegalStateException(
                        "Enum has duplicate index: Type=" + e.getClass() + ", index=" + curIdx);
            }
            map.put(curIdx, e);
        }
        return map;
    }

    /**
     * 将实现{@link IdEnum}接口的枚举转成一个Map(key:{@link IdEnum#getId()}, value:枚举对象)
     *
     * @param enums
     * @return
     */
    public static <T, E extends IdEnum<T>> Map<T, E> toMap(E[] enums) {
        Map<T, E> map = new HashMap<T, E>();

        for (E e : enums) {
            T id = e.getId();

            if (map.containsKey(id)) {
                throw new IllegalStateException(
                        "Enum has duplicate id: Type=" + e.getClass() + ", id=" + id);
            }
            map.put(id, e);
        }
        return map;
    }

    public static <T extends Enum<T>> T valueOfIgnoreCase(Class<T> enumType, String name) {
        try {
            return Enum.valueOf(enumType, name);
        } catch (IllegalArgumentException ignored) {
            // no match
        }

        T[] enumConstants = enumType.getEnumConstants();
        for (T e : enumConstants) {
            if (e.name().equalsIgnoreCase(name)) {
                return e;
            }
        }

        throw new IllegalArgumentException(
                "No enum constant " + enumType.getCanonicalName() + "." + name);
    }

    @SuppressWarnings("unchecked")
    public static <T extends IntEnum> T valueOf(Class<T> enumType, int value) {
        Method findByIdMethod = null;
        try {
            findByIdMethod = enumType.getMethod("findById", int.class);
        } catch (Exception e) {
            throw JavaUtils.sneakyThrow(e);
        }
        T rst;
        try {
            rst = (T) findByIdMethod.invoke(null, value);
        } catch (Exception e) {
            throw JavaUtils.sneakyThrow(e);
        }
        return rst;
    }

}
