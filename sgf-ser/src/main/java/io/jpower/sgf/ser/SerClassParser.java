package io.jpower.sgf.ser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.jpower.sgf.collection.DoubleValueMap;
import io.jpower.sgf.collection.FloatValueMap;
import io.jpower.sgf.collection.IntHashMap;
import io.jpower.sgf.collection.IntMap;
import io.jpower.sgf.collection.IntSet;
import io.jpower.sgf.collection.IntValueMap;
import io.jpower.sgf.collection.LongMap;
import io.jpower.sgf.collection.LongSet;
import io.jpower.sgf.collection.LongValueMap;
import io.jpower.sgf.enumtype.IntEnum;
import io.jpower.sgf.ser.annotation.AfterDeserialize;
import io.jpower.sgf.ser.annotation.AfterSerialize;
import io.jpower.sgf.ser.annotation.BeforeDeserialize;
import io.jpower.sgf.ser.annotation.BeforeSerialize;
import io.jpower.sgf.ser.annotation.Serializable;

/**
 * 用来分析有{@link Serializable}注解的类
 *
 * @author zheng.sun
 */
class SerClassParser {

    private static final SerClassParser INS = new SerClassParser();

    private ConcurrentMap<Class<?>, SerClass> cache = new ConcurrentHashMap<>();

    private ConcurrentMap<ParameterizedType, List<FieldType>> subTypeMap = new ConcurrentHashMap<>();

    private SerClassParser() {

    }

    public static SerClassParser ins() {
        return INS;
    }

    public SerClass parse(Class<?> clazz) {
        SerClass serClass = cache.get(clazz);
        if (serClass != null) {
            return serClass;
        }

        serClass = parseClazz(clazz);

        SerClass existsSerClass = cache.putIfAbsent(clazz, serClass);
        if (existsSerClass != null) {
            serClass = existsSerClass;
        }

        return serClass;
    }

    private SerClass parseClazz(Class<?> clazz) {
        // 检查
        Serializable serAnno = clazz.getAnnotation(Serializable.class);
        if (serAnno == null) {
            throw new SerializationException(clazz + " has no Serializable annotation");
        }
        if (clazz.isInterface()) {
            throw new SerializationException(clazz + " is interface");
        }
        int modifiers = clazz.getModifiers();
        if (Modifier.isAbstract(modifiers)) {
            throw new SerializationException(clazz + " is abstract");
        }
        if (!Modifier.isPublic(modifiers)) {
            throw new SerializationException(clazz + " is not public");
        }
        // 默认构造方法
        Constructor<?> constructor = null;
        try {
            constructor = clazz.getConstructor(Utils.EMPTY_CLASSES);
        } catch (NoSuchMethodException exs) {
            throw new SerializationException(clazz + " has not default constructor");
        }
        SerClass serClass = new SerClass(clazz);
        serClass.setConstructor(constructor);

        // 分析field
        IntMap<SerField> serFieldMap = new IntHashMap<>(); // key: fieldNumber
        parseField(clazz, serFieldMap);
        List<SerField> serFields = toSerFieldList(serFieldMap);
        serClass.setFields(serFields);
        serClass.setFieldMap(serFieldMap);

        // 分析before after方法
        Method beforeSerMethod = findMethods(clazz, BeforeSerialize.class);
        serClass.setBeforeSerMethod(beforeSerMethod);

        Method afterSerMethod = findMethods(clazz, AfterSerialize.class);
        serClass.setAfterSerMethod(afterSerMethod);

        Method beforeDeserMethod = findMethods(clazz, BeforeDeserialize.class);
        serClass.setBeforeDeserMethod(beforeDeserMethod);

        Method afterDeserMethod = findMethods(clazz, AfterDeserialize.class);
        serClass.setAfterDeserMethod(afterDeserMethod);

        return serClass;
    }

    private List<SerField> toSerFieldList(IntMap<SerField> serFieldMap) {
        if (serFieldMap.isEmpty()) {
            return Collections.emptyList();
        }
        List<SerField> serFields = new ArrayList<>();
        for (SerField f : serFieldMap.values()) {
            serFields.add(f);
        }
        // 排个序，fieldNumber小的排前面
        Collections.sort(serFields, new Comparator<SerField>() {

            @Override
            public int compare(SerField o1, SerField o2) {
                return o1.getNumber() - o2.getNumber();
            }
        });
        return serFields;
    }

    private void parseField(Class<?> clazz, IntMap<SerField> serFieldMap) {
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            SerField serField = parseField(clazz, f);
            if (serField == null) {
                continue; // 没有就跳过
            }
            SerField exists = serFieldMap.put(serField.getNumber(), serField);
            if (exists != null) {
                throw new SerializationException(clazz + " fieldNumber duplicate. fieldNumber="
                        + serField.getNumber() + ", field1=" + exists.getField() + ", field2="
                        + serField.getField());
            }
        }

        // 查找父类的field
        Class<?> superclass = clazz.getSuperclass();
        if (superclass == null || superclass == Object.class) {
            return;
        }
        parseField(superclass, serFieldMap);
    }

    private SerField parseField(Class<?> clazz, Field field) {
        io.jpower.sgf.ser.annotation.Field anno = field
                .getAnnotation(io.jpower.sgf.ser.annotation.Field.class);
        if (anno == null) {
            return null;
        }
        int modifiers = field.getModifiers();
        if (Modifier.isStatic(modifiers)) {
            throw new SerializationException(field + " is static");
        }
        int number = anno.value();
        Utils.checkFieldNumber(number);

        if (!field.isAccessible()) {
            field.setAccessible(true);
        }

        SerField serField = new SerField(field);
        serField.setNumber(number);

        // 寻找getter和setter
        Class<?> fieldClass = field.getType();
        String baseName = Utils.capitalize(field.getName());
        Method getter = null;
        try {
            getter = clazz.getMethod(Utils.GET_PREFIX + baseName, Utils.EMPTY_CLASSES);
        } catch (NoSuchMethodException e) {

        }
        if (getter == null) {
            if (fieldClass == boolean.class || fieldClass == Boolean.class) {
                try {
                    getter = clazz.getMethod(Utils.IS_PREFIX + baseName, Utils.EMPTY_CLASSES);
                } catch (NoSuchMethodException e) {

                }
            }
        }
        if (getter != null && checkMethodModifier(getter.getModifiers())) {
            if (getter.getReturnType() == fieldClass) { // 检查返回值类型
                serField.setGetter(getter);
            }
        }

        Method setter = null;
        try {
            setter = clazz.getMethod(Utils.SET_PREFIX + baseName, fieldClass);
        } catch (NoSuchMethodException e) {

        }
        if (setter != null && checkMethodModifier(setter.getModifiers())) {
            serField.setSetter(setter);
        }

        // intern
        boolean intern = anno.intern();
        serField.setIntern(intern);

        // intEncode
        IntEncodeType intEncodeType = anno.intEncodeType();
        serField.setIntEncodeType(intEncodeType);

        // DeSerClazz
        Class<?> deSerClazz = anno.deSerClazz();
        serField.setDeSerClazz(deSerClazz);

        // 判断类型
        FieldType fieldType = parseFieldType(field, serField);
        serField.setType(fieldType);

        return serField;
    }

    private FieldType parseFieldType(Field field, SerField serField) {
        Class<?> fieldClass = field.getType();
        FieldType fieldType = null;
        if (fieldClass == byte.class || fieldClass == Byte.class) {
            fieldType = new FieldType(fieldClass, JavaType.BYTE, fieldClass.isPrimitive(),
                    serField.getIntEncodeType().getInt8WireType());

        } else if (fieldClass == char.class || fieldClass == Character.class) {
            fieldType = new FieldType(fieldClass, JavaType.CHAR, fieldClass.isPrimitive(),
                    serField.getIntEncodeType().getInt16WireType());

        } else if (fieldClass == short.class || fieldClass == Short.class) {
            fieldType = new FieldType(fieldClass, JavaType.SHORT, fieldClass.isPrimitive(),
                    serField.getIntEncodeType().getInt16WireType());

        } else if (fieldClass == int.class || fieldClass == Integer.class) {
            fieldType = new FieldType(fieldClass, JavaType.INT, fieldClass.isPrimitive(),
                    serField.getIntEncodeType().getInt32WireType());

        } else if (fieldClass == long.class || fieldClass == Long.class) {
            fieldType = new FieldType(fieldClass, JavaType.LONG, fieldClass.isPrimitive(),
                    serField.getIntEncodeType().getInt64WireType());

        } else if (fieldClass == float.class || fieldClass == Float.class) {
            fieldType = new FieldType(fieldClass, JavaType.FLOAT, fieldClass.isPrimitive(),
                    WireFormat.WIRETYPE_FIXED32);

        } else if (fieldClass == double.class || fieldClass == Double.class) {
            fieldType = new FieldType(fieldClass, JavaType.DOUBLE, fieldClass.isPrimitive(),
                    WireFormat.WIRETYPE_FIXED64);

        } else if (fieldClass == boolean.class || fieldClass == Boolean.class) {
            fieldType = new FieldType(fieldClass, JavaType.BOOL, fieldClass.isPrimitive(),
                    WireFormat.WIRETYPE_VARINT);

        } else if (IntEnum.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.INT_ENUM,
                    serField.getIntEncodeType().getInt32WireType());

        } else if (fieldClass == byte[].class) {
            fieldType = new FieldType(fieldClass, JavaType.BYTES, WireFormat.WIRETYPE_BYTES);

        } else if (fieldClass == String.class) {
            fieldType = new FieldType(fieldClass, JavaType.STRING, WireFormat.WIRETYPE_BYTES);

        } else if (List.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.LIST, WireFormat.WIRETYPE_COLLECTION);
            parseSubFieldType(field.getGenericType(), fieldType);

        } else if (Set.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.SET, WireFormat.WIRETYPE_COLLECTION);
            parseSubFieldType(field.getGenericType(), fieldType);

        } else if (IntSet.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.INT_SET, WireFormat.WIRETYPE_COLLECTION);

        } else if (LongSet.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.LONG_SET,
                    WireFormat.WIRETYPE_COLLECTION);

        } else if (Map.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.MAP, WireFormat.WIRETYPE_MAP);
            parseSubFieldType(field.getGenericType(), fieldType);

        } else if (IntMap.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.INT_MAP, WireFormat.WIRETYPE_MAP);
            parseSubFieldType(field.getGenericType(), fieldType);

        } else if (LongMap.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.LONG_MAP, WireFormat.WIRETYPE_MAP);
            parseSubFieldType(field.getGenericType(), fieldType);

        } else if (IntValueMap.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.INT_VALUE_MAP, WireFormat.WIRETYPE_MAP);
            parseSubFieldType(field.getGenericType(), fieldType);

        } else if (LongValueMap.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.LONG_VALUE_MAP, WireFormat.WIRETYPE_MAP);
            parseSubFieldType(field.getGenericType(), fieldType);

        } else if (FloatValueMap.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.FLOAT_VALUE_MAP,
                    WireFormat.WIRETYPE_MAP);
            parseSubFieldType(field.getGenericType(), fieldType);

        } else if (DoubleValueMap.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.DOUBLE_VALUE_MAP,
                    WireFormat.WIRETYPE_MAP);
            parseSubFieldType(field.getGenericType(), fieldType);

        } else {
            fieldType = new FieldType(fieldClass, JavaType.SER_OBJECT,
                    WireFormat.WIRETYPE_SER_OBJECT);

        }
        return fieldType;
    }

    /**
     * 分析容器（如list，map等）的子类型
     *
     * @param genericType
     * @param fieldType
     */
    private void parseSubFieldType(Type genericType, FieldType fieldType) {
        if (!(genericType instanceof ParameterizedType)) {
            throw new SerializationException("Unsupported subType: " + genericType);
        }

        ParameterizedType paramType = (ParameterizedType) genericType;

        List<FieldType> subTypes = subTypeMap.get(paramType);
        if (subTypes == null) {
            // 分析
            Type[] actualTypeArguments = paramType.getActualTypeArguments();
            subTypes = new ArrayList<>(actualTypeArguments.length);

            for (Type t : actualTypeArguments) {
                FieldType subField = parseSubFieldType(t);
                subTypes.add(subField);
            }

            List<FieldType> existsSubTypes = subTypeMap.putIfAbsent(paramType, subTypes);
            if (existsSubTypes != null) {
                subTypes = existsSubTypes;
            }
        }

        fieldType.setSubTypes(subTypes);
    }

    private FieldType parseSubFieldType(Type type) {
        Class<?> fieldClass = null;
        if (type instanceof Class) {
            fieldClass = (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            fieldClass = (Class<?>) ((ParameterizedType) type).getRawType();
        }
        if (fieldClass == null) {
            throw new SerializationException("Can't parse type: " + type);
        }

        FieldType fieldType = null;
        if (fieldClass == Byte.class) {
            fieldType = new FieldType(fieldClass, JavaType.BYTE, WireFormat.WIRETYPE_VARINT);

        } else if (fieldClass == Character.class) {
            fieldType = new FieldType(fieldClass, JavaType.CHAR, WireFormat.WIRETYPE_VARINT);

        } else if (fieldClass == Short.class) {
            fieldType = new FieldType(fieldClass, JavaType.SHORT, WireFormat.WIRETYPE_VARINT);

        } else if (fieldClass == Integer.class) {
            fieldType = new FieldType(fieldClass, JavaType.INT, WireFormat.WIRETYPE_VARINT);

        } else if (fieldClass == Long.class) {
            fieldType = new FieldType(fieldClass, JavaType.LONG, WireFormat.WIRETYPE_VARINT);

        } else if (fieldClass == Float.class) {
            fieldType = new FieldType(fieldClass, JavaType.FLOAT, WireFormat.WIRETYPE_FIXED32);

        } else if (fieldClass == Double.class) {
            fieldType = new FieldType(fieldClass, JavaType.DOUBLE, WireFormat.WIRETYPE_FIXED64);

        } else if (fieldClass == Boolean.class) {
            fieldType = new FieldType(fieldClass, JavaType.BOOL, WireFormat.WIRETYPE_VARINT);

        } else if (IntEnum.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.INT_ENUM, WireFormat.WIRETYPE_VARINT);

        } else if (fieldClass == byte[].class) {
            fieldType = new FieldType(fieldClass, JavaType.BYTES, WireFormat.WIRETYPE_BYTES);

        } else if (fieldClass == String.class) {
            fieldType = new FieldType(fieldClass, JavaType.STRING, WireFormat.WIRETYPE_BYTES);

        } else if (List.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.LIST, WireFormat.WIRETYPE_COLLECTION);
            parseSubFieldType(type, fieldType);

        } else if (Set.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.SET, WireFormat.WIRETYPE_COLLECTION);
            parseSubFieldType(type, fieldType);

        } else if (IntSet.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.INT_SET, WireFormat.WIRETYPE_COLLECTION);

        } else if (LongSet.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.LONG_SET,
                    WireFormat.WIRETYPE_COLLECTION);

        } else if (Map.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.MAP, WireFormat.WIRETYPE_MAP);
            parseSubFieldType(type, fieldType);

        } else if (IntMap.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.INT_MAP, WireFormat.WIRETYPE_MAP);
            parseSubFieldType(type, fieldType);

        } else if (LongMap.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.LONG_MAP, WireFormat.WIRETYPE_MAP);
            parseSubFieldType(type, fieldType);

        } else if (IntValueMap.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.INT_VALUE_MAP, WireFormat.WIRETYPE_MAP);
            parseSubFieldType(type, fieldType);

        } else if (LongValueMap.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.LONG_VALUE_MAP, WireFormat.WIRETYPE_MAP);
            parseSubFieldType(type, fieldType);

        } else if (FloatValueMap.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.FLOAT_VALUE_MAP,
                    WireFormat.WIRETYPE_MAP);
            parseSubFieldType(type, fieldType);

        } else if (DoubleValueMap.class.isAssignableFrom(fieldClass)) {
            fieldType = new FieldType(fieldClass, JavaType.DOUBLE_VALUE_MAP,
                    WireFormat.WIRETYPE_MAP);
            parseSubFieldType(type, fieldType);

        } else {
            fieldType = new FieldType(fieldClass, JavaType.SER_OBJECT,
                    WireFormat.WIRETYPE_SER_OBJECT);

        }

        return fieldType;
    }

    private boolean checkMethodModifier(int modifiers) {
        if (Modifier.isStatic(modifiers)) {
            return false;
        }
        if (Modifier.isAbstract(modifiers)) {
            return false;
        }
        if (!Modifier.isPublic(modifiers)) {
            return false;
        }
        return true;
    }

    private Method findMethods(Class<?> clazz, Class<? extends Annotation> annoType) {
        Method[] methods = clazz.getDeclaredMethods();
        Method method = null;
        for (Method m : methods) {
            Annotation anno = m.getAnnotation(annoType);
            if (anno == null) {
                continue;
            }
            if (method != null) { // 检查重复
                throw new SerializationException(clazz + " has duplicate " + anno
                        + " method. method1=" + method + " method2=" + m);
            }
            int modifiers = m.getModifiers();
            if (Modifier.isStatic(modifiers)) {
                throw new SerializationException(anno + " method is static. method=" + m);
            }
            if (Modifier.isAbstract(modifiers)) {
                throw new SerializationException(anno + " method is abstract. method=" + m);
            }
            method = m;
        }
        return method;
    }

}
