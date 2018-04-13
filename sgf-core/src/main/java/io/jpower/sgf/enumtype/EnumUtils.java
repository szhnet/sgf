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

    private static final ConcurrentMap<Class<? extends IntEnum>, IntEnumHolder> INT_ENUM_CACHE = new
            ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<? extends Enum<?>>, TagAnnoEnumHolder> TAG_ANNO_ENUM_CACHE = new
            ConcurrentHashMap<>();

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

    public static int idOf(Enum<?> enumValue) {
        if (enumValue instanceof IntEnum) {
            return idOfIntEnum(enumValue);
        } else {
            return idOfTagAnnoEnum(enumValue);
        }
    }

    private static int idOfIntEnum(Enum<?> enumValue) {
        IntEnum intEnum = (IntEnum) enumValue;
        return intEnum.getId();
    }

    private static int idOfTagAnnoEnum(Enum<?> enumValue) {
        Class<? extends Enum<?>> enumType = enumValue.getDeclaringClass();

        TagAnnoEnumHolder holder = TAG_ANNO_ENUM_CACHE.get(enumType);
        if (holder == null) {
            holder = createTagAnnoEnumHolder(enumType);
            TagAnnoEnumHolder existsHolder = TAG_ANNO_ENUM_CACHE.putIfAbsent(enumType, holder);
            if (existsHolder != null) {
                holder = existsHolder;
            }
        }

        Integer id = holder.getEnumToId().get(enumValue);
        if (id == null) {
            throw new IllegalStateException("The enum is not mapped to id. Type=" + enumType + "enum=" + enumValue);
        }
        return id;
    }

    /**
     * 根据id值，返回对应的枚举值
     * <P>需要用{@link Tag}标记枚举字段或者实现{@link IntEnum}接口
     *
     * @param enumType
     * @param id
     * @param <E>
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E extends Enum> E valueOf(Class<E> enumType, int id) {
        if (IntEnum.class.isAssignableFrom(enumType)) {
            return (E) valueOfIntEnum((Class<? extends IntEnum>) enumType, id);
        } else if (enumType.isEnum()) {
            return (E) valueOfTagAnnoEnum(enumType, id);
        } else {
            throw new IllegalArgumentException("The enumType is not IntEnum or Enum. enumType=" + enumType);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends IntEnum> E valueOfIntEnum(Class<E> enumType, int id) {
        IntEnumHolder holder = INT_ENUM_CACHE.get(enumType);
        if (holder == null) {
            holder = createIntEnumHolder(enumType);
            IntEnumHolder existsHolder = INT_ENUM_CACHE.putIfAbsent(enumType, holder);
            if (existsHolder != null) {
                holder = existsHolder;
            }
        }
        Method findByIdMethod = holder.getFindByIdMethod();
        if (findByIdMethod != null) {
            E rst;
            try {
                rst = (E) findByIdMethod.invoke(null, id);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
            return rst;
        } else {
            Object idToEnum = holder.getIdToEnum();
            if (idToEnum instanceof IntMap) {
                IntMap<E> enumMap = (IntMap<E>) idToEnum;
                return enumMap.get(id);
            } else {
                E[] enumArray = (E[]) idToEnum;
                if (id < 0 || id >= enumArray.length) {
                    return null;
                }
                return enumArray[id];
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> E valueOfTagAnnoEnum(Class<E> enumType, int id) {
        TagAnnoEnumHolder holder = TAG_ANNO_ENUM_CACHE.get(enumType);
        if (holder == null) {
            holder = createTagAnnoEnumHolder(enumType);
            TagAnnoEnumHolder existsHolder = TAG_ANNO_ENUM_CACHE.putIfAbsent(enumType, holder);
            if (existsHolder != null) {
                holder = existsHolder;
            }
        }
        Object idToEnum = holder.getIdToEnum();
        if (idToEnum instanceof IntMap) {
            IntMap<E> enumMap = (IntMap<E>) idToEnum;
            return enumMap.get(id);
        } else {
            E[] enumArray = (E[]) idToEnum;
            if (id < 0 || id >= enumArray.length) {
                return null;
            }
            return enumArray[id];
        }
    }

    @SuppressWarnings("unchecked")
    private static IntEnumHolder createIntEnumHolder(Class<? extends IntEnum> enumType) {
        Method findByIdMethod = null;
        try {
            findByIdMethod = enumType.getMethod("findById", int.class);
        } catch (NoSuchMethodException e) {
            // no findById method
        }

        IntEnumHolder holder;
        if (findByIdMethod != null) {
            holder = new IntEnumHolder(enumType, findByIdMethod);
        } else if (enumType.isEnum()) {
            Class<? extends Enum<?>> et = (Class<? extends Enum<?>>) enumType;
            IntMap<Enum<?>> idToEnumMap = new IntHashMap<>();
            int maxId = 0;
            for (Enum<?> enumConst : et.getEnumConstants()) {
                IntEnum intEnumConst = (IntEnum) enumConst;

                int id = intEnumConst.getId();
                if (id < 0) {
                    throw new IndexOutOfBoundsException("Enum id cannot be negative: Type="
                            + enumType + ", id=" + id);
                }
                if (id > maxId) {
                    maxId = id;
                }
                if (idToEnumMap.containsKey(id)) {
                    throw new IllegalStateException(
                            "Enum has duplicate id: Type=" + enumType + ", id=" + id);
                }

                idToEnumMap.put(id, enumConst);
            }

            if (maxId > MAX_INDEX) {
                holder = new IntEnumHolder(enumType, idToEnumMap);
            } else {
                holder = new IntEnumHolder(enumType, toArray(et, idToEnumMap, maxId));
            }
        } else {
            throw new IllegalArgumentException("The enumType is not Enum. enumType=" + enumType);
        }

        return holder;
    }

    @SuppressWarnings("unchecked")
    private static TagAnnoEnumHolder createTagAnnoEnumHolder(Class<? extends Enum<?>> enumType) {
        IntMap<Enum<?>> tagToEnumMap = new IntHashMap<>();
        Map<Enum<?>, Integer> enumToTagMap = new HashMap<>();
        Field[] declaredFields = enumType.getDeclaredFields();
        int maxId = 0;
        for (Field f : declaredFields) {
            f.setAccessible(true);
            if (f.getDeclaringClass() != enumType) {
                continue;
            }
            Tag tagAnno = f.getAnnotation(Tag.class);
            if (tagAnno == null) {
                continue;
            }

            int id = tagAnno.value();
            if (id < 0) {
                throw new IndexOutOfBoundsException("Enum id cannot be negative: Type="
                        + enumType + ", id=" + id);
            }
            if (id > maxId) {
                maxId = id;
            }
            if (tagToEnumMap.containsKey(id)) {
                throw new IllegalStateException(
                        "Enum has duplicate id: Type=" + enumType + ", id=" + id);
            }

            Enum<?> fv;
            try {
                fv = (Enum<?>) f.get(null);
            } catch (IllegalAccessException e) {
                throw JavaUtils.sneakyThrow(e);
            }
            tagToEnumMap.put(id, fv);
            enumToTagMap.put(fv, id);
        }

        TagAnnoEnumHolder holder;
        if (maxId > MAX_INDEX) {
            holder = new TagAnnoEnumHolder(enumType, enumToTagMap, tagToEnumMap);
        } else {
            holder = new TagAnnoEnumHolder(enumType, enumToTagMap, toArray(enumType, tagToEnumMap, maxId));
        }
        return holder;
    }

    @SuppressWarnings("rawtypes")
    private static Enum[] toArray(Class<? extends Enum<?>> enumType, IntMap<Enum<?>> enumMap, int
            maxId) {
        Enum[] enumArray = (Enum[]) Array.newInstance(enumType, maxId + 1);
        for (IntMap.Entry<Enum<?>> en : enumMap.entrySet()) {
            int id = en.getKey();
            Enum<?> ev = en.getValue();

            Enum<?> existsEv = enumArray[id];
            if (existsEv != null) {
                throw new IllegalStateException(
                        "Enum has duplicate id: Type=" + enumType + ", id=" + id);
            }
            enumArray[id] = ev;
        }
        return enumArray;
    }

    private static class IntEnumHolder {

        private final Class<? extends IntEnum> clazz;

        private final Method findByIdMethod;

        private final Object idToEnum;

        private IntEnumHolder(Class<? extends IntEnum> clazz, Method findByIdMethod) {
            this.clazz = clazz;
            this.findByIdMethod = findByIdMethod;
            this.idToEnum = null;
        }

        private IntEnumHolder(Class<? extends IntEnum> clazz, Object tagToEnum) {
            this.clazz = clazz;
            this.findByIdMethod = null;
            this.idToEnum = tagToEnum;
        }

        Class<? extends IntEnum> getClazz() {
            return clazz;
        }

        Method getFindByIdMethod() {
            return findByIdMethod;
        }

        Object getIdToEnum() {
            return idToEnum;
        }

    }

    private static class TagAnnoEnumHolder {

        private final Class<? extends Enum<?>> clazz;

        private final Map<Enum<?>, Integer> enumToId;

        private final Object idToEnum;

        private TagAnnoEnumHolder(Class<? extends Enum<?>> clazz, Map<Enum<?>, Integer> enumToId, Object idToEnum) {
            this.clazz = clazz;
            this.enumToId = enumToId;
            this.idToEnum = idToEnum;
        }

        Class<? extends Enum<?>> getClazz() {
            return clazz;
        }

        Map<Enum<?>, Integer> getEnumToId() {
            return enumToId;
        }

        Object getIdToEnum() {
            return idToEnum;
        }

    }

}
