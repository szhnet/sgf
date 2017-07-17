package io.jpower.sgf.enumtype;

import io.jpower.sgf.collection.IntHashMap;
import io.jpower.sgf.collection.IntMap;
import io.jpower.sgf.utils.JavaUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class EnumUtils {

    private static final int MAX_INDEX = 9999;

    private static final ConcurrentMap<Class<? extends Enum<?>>, EnumHolder> ENUM_TAG_CACHE = new ConcurrentHashMap<>();

    private static Class<?> getEnumClass(Object e) {
        if (e instanceof Enum) {
            return ((Enum<?>) e).getDeclaringClass();
        } else {
            return e.getClass();
        }
    }

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
                        + getEnumClass(e) + ", index=" + curIdx);
            }

            if (curIdx > MAX_INDEX) {
                throw new IllegalStateException(
                        "Enum index is too big: Type=" + getEnumClass(e) + ", index=" + curIdx);
            }

            if (curIdx > maxIndex) {
                maxIndex = curIdx;
            }

        }

        @SuppressWarnings("unchecked")
        E[] enumArray = (E[]) Array.newInstance(enums.getClass().getComponentType(), maxIndex + 1);
        for (E e : enums) {
            int curIdx = e.getId();

            E existsEnum = enumArray[curIdx];
            if (existsEnum != null) {
                throw new IllegalStateException(
                        "Enum has duplicate index: Type=" + getEnumClass(e) + ", index=" + curIdx);
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
                        "Enum has duplicate index: Type=" + getEnumClass(e) + ", index=" + curIdx);
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
                        "Enum has duplicate id: Type=" + getEnumClass(e) + ", id=" + id);
            }
            map.put(id, e);
        }
        return map;
    }

    /**
     * 根据枚举的名字找到枚举
     * <p>
     * 与{@link Enum#valueOf(Class, String)}的区别是，此方法是忽略大小写的。
     *
     * @param enumType
     * @param name
     * @param <T>
     * @return
     */
    public static <T extends Enum<T>> T valueOfIgnoreCase(Class<T> enumType, String name) {
        try {
            return Enum.valueOf(enumType, name);
        } catch (IllegalArgumentException ignored) {
            // no match
        }

        T[] enumConsts = enumType.getEnumConstants();
        for (T e : enumConsts) {
            if (e.name().equalsIgnoreCase(name)) {
                return e;
            }
        }

        throw new IllegalArgumentException(
                "No enum constant " + enumType.getCanonicalName() + "." + name);
    }

    /**
     * 根据指定的value找到对应的IntEnum的枚举
     *
     * @param enumType
     * @param value
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends IntEnum> T valueOfIntEnum(Class<T> enumType, int value) {
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

    /**
     * 根据枚举返回枚举的tag值
     * <p>
     * 需要用{@link Tag}标记枚举字段
     *
     * @param enumValue
     * @return
     */
    public static int tag(Enum<?> enumValue) {
        Class<? extends Enum<?>> enumType = enumValue.getDeclaringClass();

        EnumHolder holder = ENUM_TAG_CACHE.get(enumType);
        if (holder == null) {
            holder = toEnumHolder(enumType);
            EnumHolder existsHolder = ENUM_TAG_CACHE.putIfAbsent(enumType, holder);
            if (existsHolder != null) {
                holder = existsHolder;
            }
        }

        Integer tag = holder.getEnumToTag().get(enumValue);
        if (tag == null) {
            throw new IllegalStateException("The enum is not mapped to tag. Type=" + enumType + "enum=" + enumValue);
        }
        return tag;
    }

    /**
     * 根据tag值，返回对应的枚举值
     * <P>需要用{@link Tag}标记枚举字段
     *
     * @param enumType
     * @param tag
     * @param <E>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> E valueOf(Class<E> enumType, int tag) {
        EnumHolder holder = ENUM_TAG_CACHE.get(enumType);
        if (holder == null) {
            holder = toEnumHolder(enumType);
            EnumHolder existsHolder = ENUM_TAG_CACHE.putIfAbsent(enumType, holder);
            if (existsHolder != null) {
                holder = existsHolder;
            }
        }
        Object tagToEnum = holder.getTagToEnum();
        if (tagToEnum instanceof IntMap) {
            IntMap<E> enumMap = (IntMap<E>) tagToEnum;
            return enumMap.get(tag);
        } else {
            E[] enumArray = (E[]) tagToEnum;
            if (tag < 0 || tag >= enumArray.length) {
                return null;
            }
            return enumArray[tag];
        }
    }

    @SuppressWarnings("unchecked")
    private static EnumHolder toEnumHolder(Class<? extends Enum<?>> enumType) {
        IntMap<Enum<?>> tagToEnumMap = new IntHashMap<>();
        Map<Enum<?>, Integer> enumToTagMap = new HashMap<>();
        Field[] declaredFields = enumType.getDeclaredFields();
        int maxTag = 0;
        for (Field f : declaredFields) {
            f.setAccessible(true);
            if (f.getDeclaringClass() != enumType) {
                continue;
            }
            Tag tagAnno = f.getAnnotation(Tag.class);
            if (tagAnno == null) {
                continue;
            }

            int tag = tagAnno.value();
            if (tag < 0) {
                throw new IndexOutOfBoundsException("Enum index cannot be negative: Type="
                        + enumType + ", tag=" + tag);
            }
            if (tag > maxTag) {
                maxTag = tag;
            }
            if (tagToEnumMap.containsKey(tag)) {
                throw new IllegalStateException(
                        "Enum has duplicate id: Type=" + enumType + ", tag=" + tag);
            }

            Enum<?> fv;
            try {
                fv = (Enum<?>) f.get(null);
            } catch (IllegalAccessException e) {
                throw JavaUtils.sneakyThrow(e);
            }
            tagToEnumMap.put(tag, fv);
            enumToTagMap.put(fv, tag);
        }

        EnumHolder holder;
        if (maxTag > MAX_INDEX) {
            holder = new EnumHolder(enumType, enumToTagMap, tagToEnumMap);
        } else {
            holder = new EnumHolder(enumType, enumToTagMap, toArray(enumType, tagToEnumMap, maxTag));
        }
        return holder;
    }

    @SuppressWarnings("rawtypes")
    private static Enum[] toArray(Class<? extends Enum<?>> enumType, IntMap<Enum<?>> enumMap, int
            maxTag) {
        @SuppressWarnings("unchecked")
        Enum[] enumArray = (Enum[]) Array.newInstance(enumType, maxTag + 1);
        for (IntMap.Entry<Enum<?>> en : enumMap.entrySet()) {
            int tag = en.getKey();
            Enum<?> ev = en.getValue();

            Enum<?> existsEv = enumArray[tag];
            if (existsEv != null) {
                throw new IllegalStateException(
                        "Enum has duplicate tag: Type=" + enumType + ", tag=" + tag);
            }
            enumArray[tag] = ev;
        }
        return enumArray;
    }

    private static class EnumHolder {

        private final Class<? extends Enum<?>> clazz;

        private final Map<Enum<?>, Integer> enumToTag;

        private final Object tagToEnum;

        private EnumHolder(Class<? extends Enum<?>> clazz, Map<Enum<?>, Integer> enumToTag, Object tagToEnum) {
            this.clazz = clazz;
            this.enumToTag = enumToTag;
            this.tagToEnum = tagToEnum;
        }

        Class<? extends Enum<?>> getClazz() {
            return clazz;
        }

        Map<Enum<?>, Integer> getEnumToTag() {
            return enumToTag;
        }

        Object getTagToEnum() {
            return tagToEnum;
        }

    }

}
