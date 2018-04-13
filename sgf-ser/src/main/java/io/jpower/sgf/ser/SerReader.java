package io.jpower.sgf.ser;

import io.jpower.sgf.collection.*;
import io.jpower.sgf.enumtype.EnumUtils;
import io.jpower.sgf.utils.JavaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static io.jpower.sgf.ser.WireFormat.*;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
class SerReader {

    private static final Logger log = LoggerFactory.getLogger(SerReader.class);

    <T> T read(DeserContext ctx, Class<T> clazz) {
        SerClass serClass = SerClassParser.ins().parse(clazz);
        @SuppressWarnings("unchecked")
        T obj = (T) readSerObject(ctx, serClass);
        return obj;
    }

    private Object readSerObject(DeserContext ctx, SerClass serClass) {
        // 构建对象
        Object obj = null;
        try {
            obj = serClass.getConstructor().newInstance(Utils.EMPTY_OBJS);
        } catch (Exception e) {
            throw JavaUtils.sneakyThrow(e);
        }

        // 调用before方法
        Method beforeDeserMethod = serClass.getBeforeDeserMethod();
        if (beforeDeserMethod != null) {
            Utils.invoke(beforeDeserMethod, obj, Utils.EMPTY_OBJS);
        }

        CodedReader reader = ctx.getReader();

        // 解码字段
        while (true) {
            int tag = reader.readTag();
            int fieldNumber = WireFormat.getTagFieldNumber(tag);
            if (fieldNumber == 0) { // 0表示结束
                break;
            }
            int wireType = WireFormat.getTagWireType(tag);
            SerField serField = serClass.getField(fieldNumber);
            if (serField == null) {
                if (log.isWarnEnabled()) {
                    log.warn("Not found field. fieldNumber={}, class={}", fieldNumber,
                            serClass.getClazz());
                }
                skip(ctx, wireType); // 不匹配，跳过
                continue;
            }
            if (wireType != serField.getType().getWireType()) {
                log.warn("WireType mismatch. Data type={}, Field type={}, Field={}", wireType,
                        serField.getType().getWireType(), serField);
                skip(ctx, wireType); // 不匹配，跳过
                continue;
            }
            try {
                readField(ctx, obj, serField);
            } catch (Exception e) {
                throw new SerializationException("Error while reading the field. Field=" + serField, e);
            }
        }

        // 调用after方法
        Method afterDeserMethod = serClass.getAfterDeserMethod();
        if (afterDeserMethod != null) {
            Utils.invoke(afterDeserMethod, obj, Utils.EMPTY_OBJS);
        }

        return obj;
    }

    private void readField(DeserContext ctx, Object obj, SerField serField) {
        if (serField.getType().isPrimitive()) {
            readPrimitiveField(ctx, obj, serField);
        } else {
            readObjectField(ctx, obj, serField);
        }
    }

    private void readObjectField(DeserContext ctx, Object obj, SerField serField) {
        FieldType fieldType = serField.getType();
        switch (fieldType.getJavaType()) {
            case BYTE:
                setValue(obj, serField, readByte(ctx, serField));
                break;
            case CHAR:
                setValue(obj, serField, readChar(ctx, serField));
                break;
            case SHORT:
                setValue(obj, serField, readShort(ctx, serField));
                break;
            case INT:
                setValue(obj, serField, readInt(ctx, serField));
                break;
            case LONG:
                setValue(obj, serField, readLong(ctx, serField));
                break;

            case FLOAT:
                setValue(obj, serField, readFloat(ctx, serField));
                break;

            case DOUBLE:
                setValue(obj, serField, readDouble(ctx, serField));
                break;

            case BOOL:
                setValue(obj, serField, readBool(ctx, serField));
                break;

            case ENUM:
                Enum<?> enumValue = readEnum(ctx, serField);
                if (enumValue != null) {
                    setValue(obj, serField, enumValue);
                }
                break;

            case BYTES:
                setValue(obj, serField, readBytes(ctx, serField));
                break;

            case STRING:
                setValue(obj, serField, readString(ctx, serField));
                break;

            case LIST:
                List<?> listValue = readList(ctx, serField);
                if (listValue != null) {
                    setValue(obj, serField, listValue);
                }
                break;

            case SET:
                Set<?> setValue = readSet(ctx, serField);
                if (setValue != null) {
                    setValue(obj, serField, setValue);
                }
                break;

            case INT_SET:
                IntSet intSetValue = readIntSet(ctx, serField);
                if (intSetValue != null) {
                    setValue(obj, serField, intSetValue);
                }
                break;

            case LONG_SET:
                LongSet longSetValue = readLongSet(ctx, serField);
                if (longSetValue != null) {
                    setValue(obj, serField, longSetValue);
                }
                break;

            case MAP:
                Map<?, ?> mapValue = readMap(ctx, serField);
                if (mapValue != null) {
                    setValue(obj, serField, mapValue);
                }
                break;

            case INT_MAP:
                IntMap<?> intMapValue = readIntMap(ctx, serField);
                if (intMapValue != null) {
                    setValue(obj, serField, intMapValue);
                }
                break;

            case LONG_MAP:
                LongMap<?> longMapValue = readLongMap(ctx, serField);
                if (longMapValue != null) {
                    setValue(obj, serField, longMapValue);
                }
                break;

            case INT_VALUE_MAP:
                IntValueMap<?> intValueMapValue = readIntValueMap(ctx, serField);
                if (intValueMapValue != null) {
                    setValue(obj, serField, intValueMapValue);
                }
                break;

            case LONG_VALUE_MAP:
                LongValueMap<?> longValueMapValue = readLongValueMap(ctx, serField);
                if (longValueMapValue != null) {
                    setValue(obj, serField, longValueMapValue);
                }
                break;

            case FLOAT_VALUE_MAP:
                FloatValueMap<?> floatValueMapValue = readFloatValueMap(ctx, serField);
                if (floatValueMapValue != null) {
                    setValue(obj, serField, floatValueMapValue);
                }
                break;

            case DOUBLE_VALUE_MAP:
                DoubleValueMap<?> doubleValueMapValue = readDoubleValueMap(ctx, serField);
                if (doubleValueMapValue != null) {
                    setValue(obj, serField, doubleValueMapValue);
                }
                break;

            case SER_OBJECT:
                Object serObjValue = readSerObject(ctx, serField);
                if (serObjValue != null) {
                    setValue(obj, serField, serObjValue);
                }
                break;

            default:
                throw new SerializationException("Unsupported type: " + fieldType);
        }
    }

    private void readPrimitiveField(DeserContext ctx, Object obj, SerField serField) {
        FieldType fieldType = serField.getType();
        switch (fieldType.getJavaType()) {
            case BYTE:
                setByteValue(obj, serField, readByte(ctx, serField));
                break;
            case CHAR:
                setCharValue(obj, serField, readChar(ctx, serField));
                break;
            case SHORT:
                setShortValue(obj, serField, readShort(ctx, serField));
                break;
            case INT:
                setIntValue(obj, serField, readInt(ctx, serField));
                break;
            case LONG:
                setLongValue(obj, serField, readLong(ctx, serField));
                break;

            case FLOAT:
                setFloatValue(obj, serField, readFloat(ctx, serField));
                break;

            case DOUBLE:
                setDoubleValue(obj, serField, readDouble(ctx, serField));
                break;

            case BOOL:
                setBoolValue(obj, serField, readBool(ctx, serField));
                break;

            default:
                throw new SerializationException("Unsupported type: " + fieldType);
        }
    }

    /* ########## setValue ########## */

    private void setValue(Object obj, SerField serField, Object value) {
        Method setter = serField.getSetter();
        if (setter != null) {
            try {
                setter.invoke(obj, value);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        } else {
            Field field = serField.getField();
            try {
                field.set(obj, value);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    private void setByteValue(Object obj, SerField serField, byte value) {
        Method setter = serField.getSetter();
        if (setter != null) {
            try {
                setter.invoke(obj, value);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        } else {
            Field field = serField.getField();
            try {
                field.setByte(obj, value);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    private void setCharValue(Object obj, SerField serField, char value) {
        Method setter = serField.getSetter();
        if (setter != null) {
            try {
                setter.invoke(obj, value);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        } else {
            Field field = serField.getField();
            try {
                field.setChar(obj, value);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    private void setShortValue(Object obj, SerField serField, short value) {
        Method setter = serField.getSetter();
        if (setter != null) {
            try {
                setter.invoke(obj, value);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        } else {
            Field field = serField.getField();
            try {
                field.setShort(obj, value);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    private void setIntValue(Object obj, SerField serField, int value) {
        Method setter = serField.getSetter();
        if (setter != null) {
            try {
                setter.invoke(obj, value);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        } else {
            Field field = serField.getField();
            try {
                field.setInt(obj, value);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    private void setLongValue(Object obj, SerField serField, long value) {
        Method setter = serField.getSetter();
        if (setter != null) {
            try {
                setter.invoke(obj, value);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        } else {
            Field field = serField.getField();
            try {
                field.setLong(obj, value);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    private void setFloatValue(Object obj, SerField serField, float value) {
        Method setter = serField.getSetter();
        if (setter != null) {
            try {
                setter.invoke(obj, value);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        } else {
            Field field = serField.getField();
            try {
                field.setFloat(obj, value);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    private void setDoubleValue(Object obj, SerField serField, double value) {
        Method setter = serField.getSetter();
        if (setter != null) {
            try {
                setter.invoke(obj, value);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        } else {
            Field field = serField.getField();
            try {
                field.setDouble(obj, value);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    private void setBoolValue(Object obj, SerField serField, boolean value) {
        Method setter = serField.getSetter();
        if (setter != null) {
            try {
                setter.invoke(obj, value);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        } else {
            Field field = serField.getField();
            try {
                field.setBoolean(obj, value);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    /* ########## read ########## */

    private byte readByte(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();
        return readInt8(reader, serField);
    }

    private char readChar(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();
        return (char) readInt16(reader, serField);
    }

    private short readShort(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();
        return readInt16(reader, serField);
    }

    private int readInt(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();
        return readInt32(reader, serField);
    }

    private long readLong(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();
        return readInt64(reader, serField);
    }

    private float readFloat(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();
        return reader.readFloat();
    }

    private double readDouble(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();
        return reader.readDouble();
    }

    private boolean readBool(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();
        return reader.readBool();
    }

    private Enum<?> readEnum(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();
        FieldType fieldType = serField.getType();
        int id = readInt32(reader, serField); // read id

        @SuppressWarnings("unchecked")
        Enum<?> value = parseEnum((Class<? extends Enum<?>>) fieldType.getRawType(), id);
        return value;
    }

    private byte[] readBytes(DeserContext ctx, SerField serField) {
        return readBytes0(ctx, serField);
    }

    private String readString(DeserContext ctx, SerField serField) {
        String value = readString0(ctx, serField);
        if (serField.isIntern()) {
            value = value.intern();
        }
        return value;
    }

    private List<?> readList(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();
        FieldType fieldType = serField.getType();

        FieldType subType = fieldType.getSubTypes().get(0); // 子类型
        int subWireType = reader.readWireType(); // sub wireType
        int size = reader.readInt32(); // 数量
        checkContainerSize(ctx, size, serField);

        if (subWireType == subType.getWireType()) {
            List<Object> value = createList(serField.getDeSerClazz());
            for (int i = 0; i < size; i++) {
                Object e = readSubValue(ctx, serField, subType);
                value.add(e);
            }
            return value;
        } else {
            if (log.isWarnEnabled()) {
                log.warn("WireType mismatch. Data type={}, Field type={}, Field={}", subWireType,
                        subType.getWireType(), serField);
            }
            // 不匹配，跳过
            for (int i = 0; i < size; i++) {
                skip(ctx, subWireType);
            }
            return null;
        }
    }

    private Set<?> readSet(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();
        FieldType fieldType = serField.getType();

        FieldType subType = fieldType.getSubTypes().get(0); // 子类型
        int subWireType = reader.readWireType(); // sub wireType
        int size = reader.readInt32(); // 数量
        checkContainerSize(ctx, size, serField);

        if (subWireType == subType.getWireType()) {
            Set<Object> value = createSet(serField.getDeSerClazz(), serField.getType());
            for (int i = 0; i < size; i++) {
                Object e = readSubValue(ctx, serField, subType);
                value.add(e);
            }
            return value;
        } else {
            if (log.isWarnEnabled()) {
                log.warn("WireType mismatch. Data type={}, Field type={}, Field={}", subWireType,
                        subType.getWireType(), serField);
            }
            // 不匹配，跳过
            for (int i = 0; i < size; i++) {
                skip(ctx, subWireType);
            }
            return null;
        }
    }

    private IntSet readIntSet(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();

        int subWireType = reader.readWireType(); // sub wireType
        int size = reader.readInt32(); // 数量
        checkContainerSize(ctx, size, serField);

        if (subWireType == WireFormat.WIRETYPE_VARINT) {
            IntSet value = createIntSet(serField.getDeSerClazz());
            for (int i = 0; i < size; i++) {
                int e = reader.readInt32();
                value.add(e);
            }
            return value;
        } else {
            if (log.isWarnEnabled()) {
                log.warn("WireType mismatch. Data type={}, Field type={}, Field={}", subWireType,
                        WireFormat.WIRETYPE_VARINT, serField);
            }
            // 不匹配，跳过
            for (int i = 0; i < size; i++) {
                skip(ctx, subWireType);
            }
            return null;
        }
    }

    private LongSet readLongSet(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();

        int subWireType = reader.readWireType(); // sub wireType
        int size = reader.readInt32(); // 数量
        checkContainerSize(ctx, size, serField);

        if (subWireType == WireFormat.WIRETYPE_VARINT) {
            LongSet value = createLongSet(serField.getDeSerClazz());
            for (int i = 0; i < size; i++) {
                long e = reader.readInt64();
                value.add(e);
            }
            return value;
        } else {
            if (log.isWarnEnabled()) {
                log.warn("WireType mismatch. Data type={}, Field type={}, Field={}", subWireType,
                        WireFormat.WIRETYPE_VARINT, serField);
            }
            // 不匹配，跳过
            for (int i = 0; i < size; i++) {
                skip(ctx, subWireType);
            }
            return null;
        }
    }

    private Map<?, ?> readMap(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();
        FieldType fieldType = serField.getType();

        FieldType keyType = fieldType.getSubTypes().get(0); // key类型
        FieldType valueType = fieldType.getSubTypes().get(1); // value类型

        int keyWireType = reader.readWireType(); // key wireType
        int valueWireType = reader.readWireType(); // value wireType
        int size = reader.readInt32(); // 数量
        checkContainerSize(ctx, size, serField);

        if (keyWireType == keyType.getWireType() && valueWireType == valueType.getWireType()) {
            Map<Object, Object> value = createMap(serField.getDeSerClazz(), fieldType);
            for (int i = 0; i < size; i++) {
                Object k = readSubValue(ctx, serField, keyType);
                Object v = readSubValue(ctx, serField, valueType);
                value.put(k, v);
            }
            return value;
        } else {
            if (log.isWarnEnabled()) {
                log.warn(
                        "WireType mismatch. Data keyType={}, Data valueType={}, Field keyType={}, Field valueType={}," +
                                " Field={}",
                        keyWireType, valueWireType, keyType.getWireType(), valueType.getWireType(),
                        serField);
            }
            // 不匹配，跳过
            for (int i = 0; i < size; i++) {
                skip(ctx, keyWireType);
                skip(ctx, valueWireType);
            }
            return null;
        }
    }

    private IntMap<?> readIntMap(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();
        FieldType fieldType = serField.getType();

        FieldType valueType = fieldType.getSubTypes().get(0); // value类型

        int keyWireType = reader.readWireType(); // key wireType
        int valueWireType = reader.readWireType(); // value wireType
        int size = reader.readInt32(); // 数量
        checkContainerSize(ctx, size, serField);

        if (keyWireType == WireFormat.WIRETYPE_VARINT && valueWireType == valueType.getWireType()) {
            IntMap<Object> value = createIntMap(serField.getDeSerClazz());
            for (int i = 0; i < size; i++) {
                int k = reader.readInt32();
                Object v = readSubValue(ctx, serField, valueType);
                value.put(k, v);
            }
            return value;
        } else {
            if (log.isWarnEnabled()) {
                log.warn(
                        "WireType mismatch. Data keyType={}, Data valueType={}, Field keyType={}, Field valueType={}," +
                                " Field={}",
                        keyWireType, valueWireType, WireFormat.WIRETYPE_VARINT,
                        valueType.getWireType(), serField);
            }
            // 不匹配，跳过
            for (int i = 0; i < size; i++) {
                skip(ctx, keyWireType);
                skip(ctx, valueWireType);
            }
            return null;
        }
    }

    private LongMap<?> readLongMap(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();
        FieldType fieldType = serField.getType();

        FieldType valueType = fieldType.getSubTypes().get(0); // value类型

        int keyWireType = reader.readWireType(); // key wireType
        int valueWireType = reader.readWireType(); // value wireType
        int size = reader.readInt32(); // 数量
        checkContainerSize(ctx, size, serField);

        if (keyWireType == WireFormat.WIRETYPE_VARINT && valueWireType == valueType.getWireType()) {
            LongMap<Object> value = createLongMap(serField.getDeSerClazz());
            for (int i = 0; i < size; i++) {
                long k = reader.readInt64();
                Object v = readSubValue(ctx, serField, valueType);
                value.put(k, v);
            }
            return value;
        } else {
            if (log.isWarnEnabled()) {
                log.warn(
                        "WireType mismatch. Data keyType={}, Data valueType={}, Field keyType={}, Field valueType={}," +
                                " Field={}",
                        keyWireType, valueWireType, WireFormat.WIRETYPE_VARINT,
                        valueType.getWireType(), serField);
            }
            // 不匹配，跳过
            for (int i = 0; i < size; i++) {
                skip(ctx, keyWireType);
                skip(ctx, valueWireType);
            }
            return null;
        }
    }

    private IntValueMap<?> readIntValueMap(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();
        FieldType fieldType = serField.getType();

        FieldType keyType = fieldType.getSubTypes().get(0); // key类型

        int keyWireType = reader.readWireType(); // key wireType
        int valueWireType = reader.readWireType(); // value wireType
        int size = reader.readInt32(); // 数量
        checkContainerSize(ctx, size, serField);

        if (keyWireType == keyType.getWireType() && valueWireType == WireFormat.WIRETYPE_VARINT) {
            IntValueMap<Object> value = createIntValueMap(serField.getDeSerClazz());
            for (int i = 0; i < size; i++) {
                Object k = readSubValue(ctx, serField, keyType);
                int v = reader.readInt32();
                value.put(k, v);
            }
            return value;
        } else {
            if (log.isWarnEnabled()) {
                log.warn(
                        "WireType mismatch. Data keyType={}, Data valueType={}, Field keyType={}, Field valueType={}," +
                                " Field={}",
                        keyWireType, valueWireType, keyType.getWireType(),
                        WireFormat.WIRETYPE_VARINT, serField);
            }
            // 不匹配，跳过
            for (int i = 0; i < size; i++) {
                skip(ctx, keyWireType);
                skip(ctx, valueWireType);
            }
            return null;
        }
    }

    private LongValueMap<?> readLongValueMap(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();
        FieldType fieldType = serField.getType();

        FieldType keyType = fieldType.getSubTypes().get(0); // key类型

        int keyWireType = reader.readWireType(); // key wireType
        int valueWireType = reader.readWireType(); // value wireType
        int size = reader.readInt32(); // 数量
        checkContainerSize(ctx, size, serField);

        if (keyWireType == keyType.getWireType() && valueWireType == WireFormat.WIRETYPE_VARINT) {
            LongValueMap<Object> value = createLongValueMap(serField.getDeSerClazz());
            for (int i = 0; i < size; i++) {
                Object k = readSubValue(ctx, serField, keyType);
                long v = reader.readInt64();
                value.put(k, v);
            }
            return value;
        } else {
            if (log.isWarnEnabled()) {
                log.warn(
                        "WireType mismatch. Data keyType={}, Data valueType={}, Field keyType={}, Field valueType={}," +
                                " Field={}",
                        keyWireType, valueWireType, keyType.getWireType(),
                        WireFormat.WIRETYPE_VARINT, serField);
            }
            // 不匹配，跳过
            for (int i = 0; i < size; i++) {
                skip(ctx, keyWireType);
                skip(ctx, valueWireType);
            }
            return null;
        }
    }

    private FloatValueMap<?> readFloatValueMap(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();
        FieldType fieldType = serField.getType();

        FieldType keyType = fieldType.getSubTypes().get(0); // key类型

        int keyWireType = reader.readWireType(); // key wireType
        int valueWireType = reader.readWireType(); // value wireType
        int size = reader.readInt32(); // 数量
        checkContainerSize(ctx, size, serField);

        if (keyWireType == keyType.getWireType() && valueWireType == WireFormat.WIRETYPE_FIXED32) {
            FloatValueMap<Object> value = createFloatValueMap(serField.getDeSerClazz());
            for (int i = 0; i < size; i++) {
                Object k = readSubValue(ctx, serField, keyType);
                float v = reader.readFloat();
                value.put(k, v);
            }
            return value;
        } else {
            if (log.isWarnEnabled()) {
                log.warn(
                        "WireType mismatch. Data keyType={}, Data valueType={}, Field keyType={}, Field valueType={}," +
                                " Field={}",
                        keyWireType, valueWireType, keyType.getWireType(),
                        WireFormat.WIRETYPE_FIXED32, serField);
            }
            // 不匹配，跳过
            for (int i = 0; i < size; i++) {
                skip(ctx, keyWireType);
                skip(ctx, valueWireType);
            }
            return null;
        }
    }

    private DoubleValueMap<?> readDoubleValueMap(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();
        FieldType fieldType = serField.getType();

        FieldType keyType = fieldType.getSubTypes().get(0); // key类型

        int keyWireType = reader.readWireType(); // key wireType
        int valueWireType = reader.readWireType(); // value wireType
        int size = reader.readInt32(); // 数量
        checkContainerSize(ctx, size, serField);

        if (keyWireType == keyType.getWireType() && valueWireType == WireFormat.WIRETYPE_FIXED64) {
            DoubleValueMap<Object> value = createDoubleValueMap(serField.getDeSerClazz());
            for (int i = 0; i < size; i++) {
                Object k = readSubValue(ctx, serField, keyType);
                double v = reader.readDouble();
                value.put(k, v);
            }
            return value;
        } else {
            if (log.isWarnEnabled()) {
                log.warn(
                        "WireType mismatch. Data keyType={}, Data valueType={}, Field keyType={}, Field valueType={}," +
                                " Field={}",
                        keyWireType, valueWireType, keyType.getWireType(),
                        WireFormat.WIRETYPE_FIXED64, serField);
            }
            // 不匹配，跳过
            for (int i = 0; i < size; i++) {
                skip(ctx, keyWireType);
                skip(ctx, valueWireType);
            }
            return null;
        }
    }

    private Object readSerObject(DeserContext ctx, SerField serField) {
        FieldType fieldType = serField.getType();
        SerClass valueSerClass = SerClassParser.ins().parse(fieldType.getRawType());
        return readSerObject(ctx, valueSerClass);
    }

    private byte readInt8(CodedReader reader, SerField serField) {
        switch (serField.getIntEncodeType()) {
            case VARINT:
                return (byte) reader.readInt32();
            case SIGNED_VARINT:
                return (byte) reader.readSInt32();
            case DEFAULT:
            case FIXED:
                return reader.readFixed8();

            default:
                throw new SerializationException(
                        "IntEncodeType mismatch: " + serField.getIntEncodeType());
        }
    }

    private short readInt16(CodedReader reader, SerField serField) {
        switch (serField.getIntEncodeType()) {
            case VARINT:
                return (short) reader.readInt32();
            case SIGNED_VARINT:
                return (short) reader.readSInt32();
            case DEFAULT:
            case FIXED:
                return reader.readFixed16();

            default:
                throw new SerializationException(
                        "IntEncodeType mismatch: " + serField.getIntEncodeType());
        }
    }

    private int readInt32(CodedReader reader, SerField serField) {
        switch (serField.getIntEncodeType()) {
            case DEFAULT:
            case VARINT:
                return reader.readInt32();
            case SIGNED_VARINT:
                return reader.readSInt32();
            case FIXED:
                return reader.readFixed32();

            default:
                throw new SerializationException(
                        "IntEncodeType mismatch: " + serField.getIntEncodeType());
        }
    }

    private long readInt64(CodedReader reader, SerField serField) {
        switch (serField.getIntEncodeType()) {
            case DEFAULT:
            case VARINT:
                return reader.readInt64();
            case SIGNED_VARINT:
                return reader.readSInt64();
            case FIXED:
                return reader.readFixed64();

            default:
                throw new SerializationException(
                        "IntEncodeType mismatch: " + serField.getIntEncodeType());
        }
    }

    /* ########## read sub ########## */

    private Object readSubValue(DeserContext ctx, SerField serField, FieldType type) {
        Object value = null;
        switch (type.getJavaType()) {
            case BYTE:
                value = readByte(ctx, serField, type);
                break;

            case CHAR:
                value = readChar(ctx, serField, type);
                break;

            case SHORT:
                value = readShort(ctx, serField, type);
                break;

            case INT:
                value = readInt(ctx, serField, type);
                break;

            case LONG:
                value = readLong(ctx, serField, type);
                break;

            case FLOAT:
                value = readFloat(ctx, serField, type);
                break;

            case DOUBLE:
                value = readDouble(ctx, serField, type);
                break;

            case BOOL:
                value = readBool(ctx, serField, type);
                break;

            case ENUM:
                value = readEnum(ctx, serField, type);
                break;

            case BYTES:
                value = readBytes(ctx, serField, type);
                break;

            case STRING:
                value = readString(ctx, serField, type);
                break;

            case LIST:
                value = readList(ctx, serField, type);
                break;

            case SET:
                value = readSet(ctx, serField, type);
                break;

            case INT_SET:
                value = readIntSet(ctx, serField, type);
                break;

            case LONG_SET:
                value = readLongSet(ctx, serField, type);
                break;

            case MAP:
                value = readMap(ctx, serField, type);
                break;

            case INT_MAP:
                value = readIntMap(ctx, serField, type);
                break;

            case LONG_MAP:
                value = readLongMap(ctx, serField, type);
                break;

            case SER_OBJECT:
                value = readSerObject(ctx, serField, type);
                break;

            default:
                throw new SerializationException("Unsupported type: " + type);
        }

        if (value == null) {
            throw new SerializationException("Deserialization failed: " + type);
        }
        return value;
    }

    private byte readByte(DeserContext ctx, SerField serField, FieldType type) {
        CodedReader reader = ctx.getReader();
        return (byte) reader.readInt32();
    }

    private char readChar(DeserContext ctx, SerField serField, FieldType type) {
        CodedReader reader = ctx.getReader();
        return (char) reader.readInt32();
    }

    private short readShort(DeserContext ctx, SerField serField, FieldType type) {
        CodedReader reader = ctx.getReader();
        return (short) reader.readInt32();
    }

    private int readInt(DeserContext ctx, SerField serField, FieldType type) {
        CodedReader reader = ctx.getReader();
        return reader.readInt32();
    }

    private long readLong(DeserContext ctx, SerField serField, FieldType type) {
        CodedReader reader = ctx.getReader();
        return reader.readInt64();
    }

    private float readFloat(DeserContext ctx, SerField serField, FieldType type) {
        CodedReader reader = ctx.getReader();
        return reader.readFloat();
    }

    private double readDouble(DeserContext ctx, SerField serField, FieldType type) {
        CodedReader reader = ctx.getReader();
        return reader.readDouble();
    }

    private boolean readBool(DeserContext ctx, SerField serField, FieldType type) {
        CodedReader reader = ctx.getReader();
        return reader.readBool();
    }

    private Enum<?> readEnum(DeserContext ctx, SerField serField, FieldType type) {
        CodedReader reader = ctx.getReader();
        int id = reader.readInt32(); // read id

        @SuppressWarnings("unchecked")
        Enum<?> value = parseEnum((Class<? extends Enum<?>>) type.getRawType(), id);
        return value;
    }

    private byte[] readBytes(DeserContext ctx, SerField serField, FieldType type) {
        return readBytes0(ctx, serField);
    }

    private String readString(DeserContext ctx, SerField serField, FieldType type) {
        return readString0(ctx, serField);
    }

    private List<?> readList(DeserContext ctx, SerField serField, FieldType type) {
        CodedReader reader = ctx.getReader();

        FieldType subType = type.getSubTypes().get(0); // 子类型
        int subWireType = reader.readWireType(); // sub wireType
        int size = reader.readInt32(); // 数量
        checkContainerSize(ctx, size, serField);

        if (subWireType == subType.getWireType()) {
            List<Object> value = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                Object e = readSubValue(ctx, serField, subType);
                value.add(e);
            }
            return value;
        } else {
            if (log.isWarnEnabled()) {
                log.warn("WireType mismatch. Data type={}, Field type={}, Field={}", subWireType,
                        subType.getWireType(), serField);
            }
            // 不匹配，跳过
            for (int i = 0; i < size; i++) {
                skip(ctx, subWireType);
            }
            return null;
        }
    }

    private Set<?> readSet(DeserContext ctx, SerField serField, FieldType type) {
        CodedReader reader = ctx.getReader();

        FieldType subType = type.getSubTypes().get(0); // 子类型
        int subWireType = reader.readWireType(); // sub wireType
        int size = reader.readInt32(); // 数量
        checkContainerSize(ctx, size, serField);

        if (subWireType == subType.getWireType()) {
            Set<Object> value = new HashSet<>();
            for (int i = 0; i < size; i++) {
                Object e = readSubValue(ctx, serField, subType);
                value.add(e);
            }
            return value;
        } else {
            if (log.isWarnEnabled()) {
                log.warn("WireType mismatch. Data type={}, Field type={}, Field={}", subWireType,
                        subType.getWireType(), serField);
            }
            // 不匹配，跳过
            for (int i = 0; i < size; i++) {
                skip(ctx, subWireType);
            }
            return null;
        }
    }

    private IntSet readIntSet(DeserContext ctx, SerField serField, FieldType type) {
        CodedReader reader = ctx.getReader();

        int subWireType = reader.readWireType(); // sub wireType
        int size = reader.readInt32(); // 数量
        checkContainerSize(ctx, size, serField);

        if (subWireType == WireFormat.WIRETYPE_VARINT) {
            IntSet value = new IntHashSet();
            for (int i = 0; i < size; i++) {
                int e = reader.readInt32();
                value.add(e);
            }
            return value;
        } else {
            if (log.isWarnEnabled()) {
                log.warn("WireType mismatch. Data type={}, Field type={}, Field={}", subWireType,
                        WireFormat.WIRETYPE_VARINT, serField);
            }
            // 不匹配，跳过
            for (int i = 0; i < size; i++) {
                skip(ctx, subWireType);
            }
            return null;
        }
    }

    private LongSet readLongSet(DeserContext ctx, SerField serField, FieldType type) {
        CodedReader reader = ctx.getReader();

        int subWireType = reader.readWireType(); // sub wireType
        int size = reader.readInt32(); // 数量
        checkContainerSize(ctx, size, serField);

        if (subWireType == WireFormat.WIRETYPE_VARINT) {
            LongSet value = new LongHashSet();
            for (int i = 0; i < size; i++) {
                long e = reader.readInt64();
                value.add(e);
            }
            return value;
        } else {
            if (log.isWarnEnabled()) {
                log.warn("WireType mismatch. Data type={}, Field type={}, Field={}", subWireType,
                        WireFormat.WIRETYPE_VARINT, serField);
            }
            // 不匹配，跳过
            for (int i = 0; i < size; i++) {
                skip(ctx, subWireType);
            }
            return null;
        }
    }

    private Map<?, ?> readMap(DeserContext ctx, SerField serField, FieldType type) {
        CodedReader reader = ctx.getReader();

        FieldType keyType = type.getSubTypes().get(0); // key类型
        FieldType valueType = type.getSubTypes().get(1); // value类型

        int keyWireType = reader.readWireType(); // key wireType
        int valueWireType = reader.readWireType(); // value wireType
        int size = reader.readInt32(); // 数量
        checkContainerSize(ctx, size, serField);

        if (keyWireType == keyType.getWireType() && valueWireType == valueType.getWireType()) {
            Map<Object, Object> value = new HashMap<>();
            for (int i = 0; i < size; i++) {
                Object k = readSubValue(ctx, serField, keyType);
                Object v = readSubValue(ctx, serField, valueType);
                value.put(k, v);
            }
            return value;
        } else {
            if (log.isWarnEnabled()) {
                log.warn(
                        "WireType mismatch. Field={}, Data keyType={}, Data valueType={}, Field keyType={}, Field " +
                                "valueType={}",
                        serField, keyWireType, valueWireType, keyType.getWireType(),
                        valueType.getWireType());
            }
            // 不匹配，跳过
            for (int i = 0; i < size; i++) {
                skip(ctx, keyWireType);
                skip(ctx, valueWireType);
            }
            return null;
        }
    }

    private IntMap<?> readIntMap(DeserContext ctx, SerField serField, FieldType type) {
        CodedReader reader = ctx.getReader();

        FieldType valueType = type.getSubTypes().get(0); // value类型

        int keyWireType = reader.readWireType(); // key wireType
        int valueWireType = reader.readWireType(); // value wireType
        int size = reader.readInt32(); // 数量
        checkContainerSize(ctx, size, serField);

        if (keyWireType == WireFormat.WIRETYPE_VARINT && valueWireType == valueType.getWireType()) {
            IntMap<Object> value = new IntHashMap<>();
            for (int i = 0; i < size; i++) {
                int k = reader.readInt32();
                Object v = readSubValue(ctx, serField, valueType);
                value.put(k, v);
            }
            return value;
        } else {
            // 不匹配，跳过
            if (log.isWarnEnabled()) {
                log.warn(
                        "WireType mismatch. Field={}, Data keyType={}, Data valueType={}, Field keyType={}, Field " +
                                "valueType={}",
                        serField, keyWireType, valueWireType, WireFormat.WIRETYPE_VARINT,
                        valueType.getWireType());
            }
            for (int i = 0; i < size; i++) {
                skip(ctx, keyWireType);
                skip(ctx, valueWireType);
            }
            return null;
        }
    }

    private LongMap<?> readLongMap(DeserContext ctx, SerField serField, FieldType type) {
        CodedReader reader = ctx.getReader();

        FieldType valueType = type.getSubTypes().get(0); // value类型

        int keyWireType = reader.readWireType(); // key wireType
        int valueWireType = reader.readWireType(); // value wireType
        int size = reader.readInt32(); // 数量
        checkContainerSize(ctx, size, serField);

        if (keyWireType == WireFormat.WIRETYPE_VARINT && valueWireType == valueType.getWireType()) {
            LongMap<Object> value = new LongHashMap<>();
            for (int i = 0; i < size; i++) {
                long k = reader.readInt64();
                Object v = readSubValue(ctx, serField, valueType);
                value.put(k, v);
            }
            return value;
        } else {
            if (log.isWarnEnabled()) {
                log.warn(
                        "WireType mismatch. Field={}, Data keyType={}, Data valueType={}, Field keyType={}, Field " +
                                "valueType={}",
                        serField, keyWireType, valueWireType, WireFormat.WIRETYPE_VARINT,
                        valueType.getWireType());
            }
            // 不匹配，跳过
            for (int i = 0; i < size; i++) {
                skip(ctx, keyWireType);
                skip(ctx, valueWireType);
            }
            return null;
        }
    }

    private Object readSerObject(DeserContext ctx, SerField serField, FieldType type) {
        SerClass valueSerClass = SerClassParser.ins().parse(type.getRawType());
        return readSerObject(ctx, valueSerClass);
    }

    /* ########## skip ########## */

    private void skip(DeserContext ctx, int wireType) {
        switch (wireType) {
            case WireFormat.WIRETYPE_VARINT:
                skipVarint(ctx);
                break;

            case WireFormat.WIRETYPE_FIXED8:
                skipFixed8(ctx);
                break;

            case WireFormat.WIRETYPE_FIXED16:
                skipFixed16(ctx);
                break;

            case WireFormat.WIRETYPE_FIXED32:
                skipFixed32(ctx);
                break;

            case WireFormat.WIRETYPE_FIXED64:
                skipFixed64(ctx);
                break;

            case WireFormat.WIRETYPE_BYTES:
                skipBytes(ctx);
                break;

            case WireFormat.WIRETYPE_COLLECTION:
                skipCollection(ctx);
                break;

            case WireFormat.WIRETYPE_MAP:
                skipMap(ctx);
                break;

            case WireFormat.WIRETYPE_SER_OBJECT:
                skipSerObject(ctx);
                break;

            default:
                throw new SerializationException("Unsupported wireType: " + wireType);
        }
    }

    private void skipVarint(DeserContext ctx) {
        CodedReader reader = ctx.getReader();
        reader.skipRawVarint();
    }

    private void skipFixed8(DeserContext ctx) {
        CodedReader reader = ctx.getReader();
        reader.skipRawBytes(1);
    }

    private void skipFixed16(DeserContext ctx) {
        CodedReader reader = ctx.getReader();
        reader.skipRawBytes(FIXED_16_SIZE);
    }

    private void skipFixed32(DeserContext ctx) {
        CodedReader reader = ctx.getReader();
        reader.skipRawBytes(FIXED_32_SIZE);
    }

    private void skipFixed64(DeserContext ctx) {
        CodedReader reader = ctx.getReader();
        reader.skipRawBytes(FIXED_64_SIZE);
    }

    private void skipBytes(DeserContext ctx) {
        CodedReader reader = ctx.getReader();
        int size = reader.readInt32();
        reader.skipRawBytes(size);
    }

    private void skipCollection(DeserContext ctx) {
        CodedReader reader = ctx.getReader();

        int subWireType = reader.readWireType(); // sub wireType
        int size = reader.readInt32(); // 数量

        for (int i = 0; i < size; i++) {
            skip(ctx, subWireType);
        }
    }

    private void skipMap(DeserContext ctx) {
        CodedReader reader = ctx.getReader();

        int keyWireType = reader.readWireType(); // key wireType
        int valueWireType = reader.readWireType(); // value wireType
        int size = reader.readInt32(); // 数量

        for (int i = 0; i < size; i++) {
            skip(ctx, keyWireType);
            skip(ctx, valueWireType);
        }
    }

    private void skipSerObject(DeserContext ctx) {
        CodedReader reader = ctx.getReader();
        // 跳过字段
        while (true) {
            int tag = reader.readTag();
            int fieldNumber = WireFormat.getTagFieldNumber(tag);
            if (fieldNumber == 0) {
                break;
            }
            int wireType = WireFormat.getTagWireType(tag);
            skip(ctx, wireType);
        }
    }

    /* ########## 其他 ########## */

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Enum<?> parseEnum(Class<? extends Enum<?>> clazz, int id) {
        return EnumUtils.valueOf((Class) clazz, id);
    }

    private byte[] readBytes0(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();
        int size = reader.readInt32();

        int sizeLimit = ctx.getByteSizeLimit();
        if (sizeLimit != Ser.NO_SIZE_LIMIT && size > sizeLimit) {
            throw new SerializationException("Bytes size exceeded max allowed. size=" + size
                    + ", limit=" + sizeLimit + ", Field=" + serField);
        }

        return reader.readBytes(size);
    }

    private String readString0(DeserContext ctx, SerField serField) {
        CodedReader reader = ctx.getReader();
        int size = reader.readInt32();

        int sizeLimit = ctx.getByteSizeLimit();
        if (sizeLimit != Ser.NO_SIZE_LIMIT && size > sizeLimit) {
            throw new SerializationException("String size exceeded max allowed. size=" + size
                    + ", limit=" + sizeLimit + ", Field=" + serField);
        }

        return reader.readString(size);
    }

    private void checkContainerSize(DeserContext ctx, int size, SerField serField) {
        if (size < 0) {
            throw new SerializationException("Container size < 0: " + size);
        }
        int sizeLimit = ctx.getContainerSizeLimit();
        if (sizeLimit != Ser.NO_SIZE_LIMIT && size > sizeLimit) {
            throw new SerializationException("Container size exceeded max allowed. size=" + size
                    + ", limit=" + sizeLimit + ", Field=" + serField);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> createList(Class<?> clazz) {
        if (clazz == null || clazz == void.class || clazz == Void.class) {
            return new ArrayList<>();
        }

        if (!List.class.isAssignableFrom(clazz)) {
            throw new SerializationException(clazz + " is not List");
        }

        try {
            return (List<T>) clazz.newInstance();
        } catch (Exception e) {
            throw JavaUtils.sneakyThrow(e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> Set<T> createSet(Class<?> clazz, FieldType fieldType) {
        if (clazz == null || clazz == void.class || clazz == Void.class) {
            return new HashSet<>();
        }

        if (!Set.class.isAssignableFrom(clazz)) {
            throw new SerializationException(clazz + " is not Set");
        }

        if (EnumSet.class.isAssignableFrom(clazz)) {
            FieldType subType = fieldType.getSubTypes().get(0);
            Class<? extends Enum> enumClazz = (Class<? extends Enum>) subType.getRawType();
            EnumSet enumSet = EnumSet.noneOf(enumClazz);
            return enumSet;
        }

        try {
            return (Set<T>) clazz.newInstance();
        } catch (Exception e) {
            throw JavaUtils.sneakyThrow(e);
        }
    }

    private IntSet createIntSet(Class<?> clazz) {
        if (clazz == null || clazz == void.class || clazz == Void.class) {
            return new IntHashSet();
        }

        if (!IntSet.class.isAssignableFrom(clazz)) {
            throw new SerializationException(clazz + " is not IntSet");
        }

        try {
            return (IntSet) clazz.newInstance();
        } catch (Exception e) {
            throw JavaUtils.sneakyThrow(e);
        }
    }

    private LongSet createLongSet(Class<?> clazz) {
        if (clazz == null || clazz == void.class || clazz == Void.class) {
            return new LongHashSet();
        }

        if (!LongSet.class.isAssignableFrom(clazz)) {
            throw new SerializationException(clazz + " is not LongSet");
        }

        try {
            return (LongSet) clazz.newInstance();
        } catch (Exception e) {
            throw JavaUtils.sneakyThrow(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <K, V> Map<K, V> createMap(Class<?> clazz, FieldType fieldType) {
        if (clazz == null || clazz == void.class || clazz == Void.class) {
            return new HashMap<>();
        }

        if (!Map.class.isAssignableFrom(clazz)) {
            throw new SerializationException(clazz + " is not Map");
        }

        if (EnumMap.class.isAssignableFrom(clazz)) {
            Class<?> keyClazz = fieldType.getSubTypes().get(0).getRawType();
            try {
                Constructor<?> constructor = clazz.getConstructor(Class.class);
                return (Map<K, V>) constructor.newInstance(keyClazz);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }

        try {
            return (Map<K, V>) clazz.newInstance();
        } catch (Exception e) {
            throw JavaUtils.sneakyThrow(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <V> IntMap<V> createIntMap(Class<?> clazz) {
        if (clazz == null || clazz == void.class || clazz == Void.class) {
            return new IntHashMap<>();
        }

        if (!IntMap.class.isAssignableFrom(clazz)) {
            throw new SerializationException(clazz + " is not IntMap");
        }

        try {
            return (IntMap<V>) clazz.newInstance();
        } catch (Exception e) {
            throw JavaUtils.sneakyThrow(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <V> LongMap<V> createLongMap(Class<?> clazz) {
        if (clazz == null || clazz == void.class || clazz == Void.class) {
            return new LongHashMap<>();
        }

        if (!LongMap.class.isAssignableFrom(clazz)) {
            throw new SerializationException(clazz + " is not LongMap");
        }

        try {
            return (LongMap<V>) clazz.newInstance();
        } catch (Exception e) {
            throw JavaUtils.sneakyThrow(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <K> IntValueMap<K> createIntValueMap(Class<?> clazz) {
        if (clazz == null || clazz == void.class || clazz == Void.class) {
            return new IntValueHashMap<>();
        }

        if (!IntValueMap.class.isAssignableFrom(clazz)) {
            throw new SerializationException(clazz + " is not IntValueMap");
        }

        try {
            return (IntValueMap<K>) clazz.newInstance();
        } catch (Exception e) {
            throw JavaUtils.sneakyThrow(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <K> LongValueMap<K> createLongValueMap(Class<?> clazz) {
        if (clazz == null || clazz == void.class || clazz == Void.class) {
            return new LongValueHashMap<>();
        }

        if (!LongValueMap.class.isAssignableFrom(clazz)) {
            throw new SerializationException(clazz + " is not LongValueMap");
        }

        try {
            return (LongValueMap<K>) clazz.newInstance();
        } catch (Exception e) {
            throw JavaUtils.sneakyThrow(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <K> FloatValueMap<K> createFloatValueMap(Class<?> clazz) {
        if (clazz == null || clazz == void.class || clazz == Void.class) {
            return new FloatValueHashMap<>();
        }

        if (!FloatValueMap.class.isAssignableFrom(clazz)) {
            throw new SerializationException(clazz + " is not FloatValueMap");
        }

        try {
            return (FloatValueMap<K>) clazz.newInstance();
        } catch (Exception e) {
            throw JavaUtils.sneakyThrow(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <K> DoubleValueMap<K> createDoubleValueMap(Class<?> clazz) {
        if (clazz == null || clazz == void.class || clazz == Void.class) {
            return new DoubleValueHashMap<>();
        }

        if (!DoubleValueMap.class.isAssignableFrom(clazz)) {
            throw new SerializationException(clazz + " is not DoubleValueMap");
        }

        try {
            return (DoubleValueMap<K>) clazz.newInstance();
        } catch (Exception e) {
            throw JavaUtils.sneakyThrow(e);
        }
    }

}
