package io.jpower.sgf.ser;

import io.jpower.sgf.collection.*;
import io.jpower.sgf.enumtype.EnumUtils;
import io.jpower.sgf.enumtype.IntEnum;
import io.jpower.sgf.utils.JavaUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
class SerWriter {

    void write(SerContext ctx, Object obj) {
        Class<?> clazz = obj.getClass();
        SerClass serClass = SerClassParser.ins().parse(clazz);
        writeSerObject(ctx, obj, serClass);
    }

    private void writeSerObject(SerContext ctx, Object obj, SerClass serClass) {
        // 调用before方法
        Method beforeSerMethod = serClass.getBeforeSerMethod();
        if (beforeSerMethod != null) {
            Utils.invoke(beforeSerMethod, obj, Utils.EMPTY_OBJS);
        }

        CodedWriter writer = ctx.getWriter();

        // 编码字段
        List<SerField> serFields = serClass.getFields();
        for (SerField serField : serFields) {
            writeField(ctx, obj, serField);
        }
        // 字段结束
        writer.writeStop();

        // 调用after方法
        Method afterSerMethod = serClass.getAfterSerMethod();
        if (afterSerMethod != null) {
            Utils.invoke(afterSerMethod, obj, Utils.EMPTY_OBJS);
        }
    }

    private void writeField(SerContext ctx, Object obj, SerField serField) {
        if (serField.getType().isPrimitive()) {
            writePrimitiveField(ctx, obj, serField);
        } else {
            writeObjectField(ctx, obj, serField);
        }
    }

    private void writeObjectField(SerContext ctx, Object obj, SerField serField) {
        FieldType fieldType = serField.getType();
        Object value = getValue(obj, serField);
        if (value == null) {
            return; // 跳过null
        }
        switch (fieldType.getJavaType()) {
            case BYTE:
                writeByte(ctx, serField, ((Byte) value).byteValue());
                break;

            case CHAR:
                writeChar(ctx, serField, ((Character) value).charValue());
                break;

            case SHORT:
                writeShort(ctx, serField, ((Short) value).shortValue());
                break;

            case INT:
                writeInt(ctx, serField, ((Integer) value).intValue());
                break;

            case LONG:
                writeLong(ctx, serField, ((Long) value).longValue());
                break;

            case FLOAT:
                writeFloat(ctx, serField, ((Float) value).floatValue());
                break;

            case DOUBLE:
                writeDouble(ctx, serField, ((Double) value).doubleValue());
                break;

            case BOOL:
                writeBoolean(ctx, serField, ((Boolean) value).booleanValue());
                break;

            case INT_ENUM:
                writeIntEnum(ctx, serField, (IntEnum) value);
                break;

            case ENUM:
                writeEnum(ctx, serField, (Enum<?>) value);
                break;

            case BYTES:
                writeBytes(ctx, serField, (byte[]) value);
                break;

            case STRING:
                writeString(ctx, serField, (String) value);
                break;

            case LIST:
            case SET:
                writeCollection(ctx, serField, (Collection<?>) value);
                break;

            case INT_SET:
                writeIntSet(ctx, serField, (IntSet) value);
                break;

            case LONG_SET:
                writeLongSet(ctx, serField, (LongSet) value);
                break;

            case MAP:
                writeMap(ctx, serField, (Map<?, ?>) value);
                break;

            case INT_MAP:
                writeIntMap(ctx, serField, (IntMap<?>) value);
                break;

            case LONG_MAP:
                writeLongMap(ctx, serField, (LongMap<?>) value);
                break;

            case INT_VALUE_MAP:
                writeIntValueMap(ctx, serField, (IntValueMap<?>) value);
                break;

            case LONG_VALUE_MAP:
                writeLongValueMap(ctx, serField, (LongValueMap<?>) value);
                break;

            case FLOAT_VALUE_MAP:
                writeFloatValueMap(ctx, serField, (FloatValueMap<?>) value);
                break;

            case DOUBLE_VALUE_MAP:
                writeDoubleValueMap(ctx, serField, (DoubleValueMap<?>) value);
                break;

            case SER_OBJECT:
                writeSerObject(ctx, serField, value);
                break;

            default:
                throw new SerializationException("Unsupported type: " + fieldType);
        }
    }

    private void writePrimitiveField(SerContext ctx, Object obj, SerField serField) {
        FieldType fieldType = serField.getType();
        switch (fieldType.getJavaType()) {
            case BYTE:
                writeByte(ctx, serField, getByteValue(obj, serField));
                break;

            case CHAR:
                writeChar(ctx, serField, getCharValue(obj, serField));
                break;

            case SHORT:
                writeShort(ctx, serField, getShortValue(obj, serField));
                break;

            case INT:
                writeInt(ctx, serField, getIntValue(obj, serField));
                break;

            case LONG:
                writeLong(ctx, serField, getLongValue(obj, serField));
                break;

            case FLOAT:
                writeFloat(ctx, serField, getFloatValue(obj, serField));
                break;

            case DOUBLE:
                writeDouble(ctx, serField, getDoubleValue(obj, serField));
                break;

            case BOOL:
                writeBoolean(ctx, serField, getBoolValue(obj, serField));
                break;

            default:
                throw new SerializationException("Unsupported type: " + fieldType);
        }
    }

    /* ########## getValue ########## */

    private Object getValue(Object obj, SerField serField) {
        Method getter = serField.getGetter();
        if (getter != null) {
            try {
                return getter.invoke(obj, Utils.EMPTY_OBJS);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        } else {
            Field field = serField.getField();
            try {
                return field.get(obj);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    private byte getByteValue(Object obj, SerField serField) {
        Method getter = serField.getGetter();
        if (getter != null) {
            try {
                return (Byte) getter.invoke(obj, Utils.EMPTY_OBJS);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        } else {
            Field field = serField.getField();
            try {
                return field.getByte(obj);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    private char getCharValue(Object obj, SerField serField) {
        Method getter = serField.getGetter();
        if (getter != null) {
            try {
                return (Character) getter.invoke(obj, Utils.EMPTY_OBJS);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        } else {
            Field field = serField.getField();
            try {
                return field.getChar(obj);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    private short getShortValue(Object obj, SerField serField) {
        Method getter = serField.getGetter();
        if (getter != null) {
            try {
                return (Short) getter.invoke(obj, Utils.EMPTY_OBJS);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        } else {
            Field field = serField.getField();
            try {
                return field.getShort(obj);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    private int getIntValue(Object obj, SerField serField) {
        Method getter = serField.getGetter();
        if (getter != null) {
            try {
                return (Integer) getter.invoke(obj, Utils.EMPTY_OBJS);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        } else {
            Field field = serField.getField();
            try {
                return field.getInt(obj);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    private long getLongValue(Object obj, SerField serField) {
        Method getter = serField.getGetter();
        if (getter != null) {
            try {
                return (Long) getter.invoke(obj, Utils.EMPTY_OBJS);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        } else {
            Field field = serField.getField();
            try {
                return field.getLong(obj);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    private float getFloatValue(Object obj, SerField serField) {
        Method getter = serField.getGetter();
        if (getter != null) {
            try {
                return (Float) getter.invoke(obj, Utils.EMPTY_OBJS);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        } else {
            Field field = serField.getField();
            try {
                return field.getFloat(obj);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    private double getDoubleValue(Object obj, SerField serField) {
        Method getter = serField.getGetter();
        if (getter != null) {
            try {
                return (Double) getter.invoke(obj, Utils.EMPTY_OBJS);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        } else {
            Field field = serField.getField();
            try {
                return field.getDouble(obj);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    private boolean getBoolValue(Object obj, SerField serField) {
        Method getter = serField.getGetter();
        if (getter != null) {
            try {
                return (Boolean) getter.invoke(obj, Utils.EMPTY_OBJS);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        } else {
            Field field = serField.getField();
            try {
                return field.getBoolean(obj);
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    /* ########## write ########## */

    private void writeByte(SerContext ctx, SerField serField, byte value) {
        CodedWriter writer = ctx.getWriter();
        writeInt8(writer, serField, value);
    }

    private void writeChar(SerContext ctx, SerField serField, char value) {
        CodedWriter writer = ctx.getWriter();
        writeInt16(writer, serField, value);
    }

    private void writeShort(SerContext ctx, SerField serField, short value) {
        CodedWriter writer = ctx.getWriter();
        writeInt16(writer, serField, value);
    }

    private void writeInt(SerContext ctx, SerField serField, int value) {
        CodedWriter writer = ctx.getWriter();
        writeInt32(writer, serField, value);
    }

    private void writeLong(SerContext ctx, SerField serField, long value) {
        CodedWriter writer = ctx.getWriter();
        writeInt64(writer, serField, value);
    }

    private void writeFloat(SerContext ctx, SerField serField, float value) {
        CodedWriter writer = ctx.getWriter();
        writer.writeFloat(serField.getNumber(), value);
    }

    private void writeDouble(SerContext ctx, SerField serField, double value) {
        CodedWriter writer = ctx.getWriter();
        writer.writeDouble(serField.getNumber(), value);
    }

    private void writeBoolean(SerContext ctx, SerField serField, boolean value) {
        CodedWriter writer = ctx.getWriter();
        writer.writeBool(serField.getNumber(), value);
    }

    private void writeIntEnum(SerContext ctx, SerField serField, IntEnum value) {
        CodedWriter writer = ctx.getWriter();
        writeInt32(writer, serField, value.getId()); // write id
    }

    private void writeEnum(SerContext ctx, SerField serField, Enum<?> value) {
        CodedWriter writer = ctx.getWriter();
        writeInt32(writer, serField, EnumUtils.tag(value)); // write id
    }

    private void writeBytes(SerContext ctx, SerField serField, byte[] value) {
        CodedWriter writer = ctx.getWriter();
        writer.writeBytes(serField.getNumber(), value);
    }

    private void writeString(SerContext ctx, SerField serField, String value) {
        CodedWriter writer = ctx.getWriter();
        writer.writeString(serField.getNumber(), value);
    }

    private void writeCollection(SerContext ctx, SerField serField, Collection<?> value) {
        CodedWriter writer = ctx.getWriter();
        FieldType fieldType = serField.getType();

        writer.writeTag(serField.getNumber(), fieldType.getWireType());

        FieldType subType = fieldType.getSubTypes().get(0);
        writer.writeWireType(subType.getWireType()); // 子类型
        writer.writeInt32NoTag(value.size()); // 数量

        for (Object e : value) {
            writeSubValue(ctx, serField, subType, e);
        }
    }

    private void writeIntSet(SerContext ctx, SerField serField, IntSet value) {
        CodedWriter writer = ctx.getWriter();
        FieldType fieldType = serField.getType();

        writer.writeTag(serField.getNumber(), fieldType.getWireType());

        writer.writeWireType(WireFormat.WIRETYPE_VARINT); // sub wireType
        writer.writeInt32NoTag(value.size()); // 数量

        IntIterator itr = value.iterator();
        while (itr.hasNext()) {
            int e = itr.next();
            writer.writeInt32NoTag(e);
        }
    }

    private void writeLongSet(SerContext ctx, SerField serField, LongSet value) {
        CodedWriter writer = ctx.getWriter();
        FieldType fieldType = serField.getType();

        writer.writeTag(serField.getNumber(), fieldType.getWireType());

        writer.writeWireType(WireFormat.WIRETYPE_VARINT); // sub wireType
        writer.writeInt32NoTag(value.size()); // 数量

        LongIterator itr = value.iterator();
        while (itr.hasNext()) {
            long e = itr.next();
            writer.writeInt64NoTag(e);
        }
    }

    private void writeMap(SerContext ctx, SerField serField, Map<?, ?> value) {
        CodedWriter writer = ctx.getWriter();
        FieldType fieldType = serField.getType();

        writer.writeTag(serField.getNumber(), fieldType.getWireType());

        FieldType keyType = fieldType.getSubTypes().get(0);
        FieldType valueType = fieldType.getSubTypes().get(1);
        writer.writeWireType(keyType.getWireType()); // key wireType
        writer.writeWireType(valueType.getWireType()); // value wireType
        writer.writeInt32NoTag(value.size()); // 数量

        for (Map.Entry<?, ?> e : value.entrySet()) {
            writeSubValue(ctx, serField, keyType, e.getKey());
            writeSubValue(ctx, serField, valueType, e.getValue());
        }
    }

    private void writeIntMap(SerContext ctx, SerField serField, IntMap<?> value) {
        CodedWriter writer = ctx.getWriter();
        FieldType fieldType = serField.getType();

        writer.writeTag(serField.getNumber(), fieldType.getWireType());

        FieldType valueType = fieldType.getSubTypes().get(0);
        writer.writeWireType(WireFormat.WIRETYPE_VARINT); // key wireType
        writer.writeWireType(valueType.getWireType()); // value wireType
        writer.writeInt32NoTag(value.size()); // 数量

        for (IntMap.Entry<?> e : value.entrySet()) {
            writer.writeInt32NoTag(e.getKey());
            writeSubValue(ctx, serField, valueType, e.getValue());
        }
    }

    private void writeLongMap(SerContext ctx, SerField serField, LongMap<?> value) {
        CodedWriter writer = ctx.getWriter();
        FieldType fieldType = serField.getType();

        writer.writeTag(serField.getNumber(), fieldType.getWireType());

        FieldType valueType = fieldType.getSubTypes().get(0);
        writer.writeWireType(WireFormat.WIRETYPE_VARINT); // key wireType
        writer.writeWireType(valueType.getWireType()); // value wireType
        writer.writeInt32NoTag(value.size()); // 数量

        for (LongMap.Entry<?> e : value.entrySet()) {
            writer.writeInt64NoTag(e.getKey());
            writeSubValue(ctx, serField, valueType, e.getValue());
        }
    }

    private void writeIntValueMap(SerContext ctx, SerField serField, IntValueMap<?> value) {
        CodedWriter writer = ctx.getWriter();
        FieldType fieldType = serField.getType();

        writer.writeTag(serField.getNumber(), fieldType.getWireType());

        FieldType keyType = fieldType.getSubTypes().get(0);
        writer.writeWireType(keyType.getWireType()); // key wireType
        writer.writeWireType(WireFormat.WIRETYPE_VARINT); // value wireType
        writer.writeInt32NoTag(value.size()); // 数量

        for (IntValueMap.Entry<?> e : value.entrySet()) {
            writeSubValue(ctx, serField, keyType, e.getKey());
            writer.writeInt32NoTag(e.getValue());
        }
    }

    private void writeLongValueMap(SerContext ctx, SerField serField, LongValueMap<?> value) {
        CodedWriter writer = ctx.getWriter();
        FieldType fieldType = serField.getType();

        writer.writeTag(serField.getNumber(), fieldType.getWireType());

        FieldType keyType = fieldType.getSubTypes().get(0);
        writer.writeWireType(keyType.getWireType()); // key wireType
        writer.writeWireType(WireFormat.WIRETYPE_VARINT); // value wireType
        writer.writeInt32NoTag(value.size()); // 数量

        for (LongValueMap.Entry<?> e : value.entrySet()) {
            writeSubValue(ctx, serField, keyType, e.getKey());
            writer.writeInt64NoTag(e.getValue());
        }
    }

    private void writeFloatValueMap(SerContext ctx, SerField serField, FloatValueMap<?> value) {
        CodedWriter writer = ctx.getWriter();
        FieldType fieldType = serField.getType();

        writer.writeTag(serField.getNumber(), fieldType.getWireType());

        FieldType keyType = fieldType.getSubTypes().get(0);
        writer.writeWireType(keyType.getWireType()); // key wireType
        writer.writeWireType(WireFormat.WIRETYPE_FIXED32); // value wireType
        writer.writeInt32NoTag(value.size()); // 数量

        for (FloatValueMap.Entry<?> e : value.entrySet()) {
            writeSubValue(ctx, serField, keyType, e.getKey());
            writer.writeFloatNoTag(e.getValue());
        }
    }

    private void writeDoubleValueMap(SerContext ctx, SerField serField, DoubleValueMap<?> value) {
        CodedWriter writer = ctx.getWriter();
        FieldType fieldType = serField.getType();

        writer.writeTag(serField.getNumber(), fieldType.getWireType());

        FieldType keyType = fieldType.getSubTypes().get(0);
        writer.writeWireType(keyType.getWireType()); // key wireType
        writer.writeWireType(WireFormat.WIRETYPE_FIXED64); // value wireType
        writer.writeInt32NoTag(value.size()); // 数量

        for (DoubleValueMap.Entry<?> e : value.entrySet()) {
            writeSubValue(ctx, serField, keyType, e.getKey());
            writer.writeDoubleNoTag(e.getValue());
        }
    }

    private void writeSerObject(SerContext ctx, SerField serField, Object value) {
        CodedWriter writer = ctx.getWriter();
        FieldType fieldType = serField.getType();

        writer.writeTag(serField.getNumber(), fieldType.getWireType());

        SerClass valueSerClass = SerClassParser.ins().parse(value.getClass());

        writeSerObject(ctx, value, valueSerClass);
    }

    private void writeInt8(CodedWriter writer, SerField serField, int value) {
        int fieldNumber = serField.getNumber();
        switch (serField.getIntEncodeType()) {
            case VARINT:
                writer.writeInt32(fieldNumber, value);
                break;
            case SIGNED_VARINT:
                writer.writeSInt32(fieldNumber, value);
                break;
            case DEFAULT:
            case FIXED:
                writer.writeFixed8(fieldNumber, value);
                break;

            default:
                throw new SerializationException(
                        "IntEncodeType mismatch : " + serField.getIntEncodeType());
        }
    }

    private void writeInt16(CodedWriter writer, SerField serField, int value) {
        int fieldNumber = serField.getNumber();
        switch (serField.getIntEncodeType()) {
            case VARINT:
                writer.writeInt32(fieldNumber, value);
                break;
            case SIGNED_VARINT:
                writer.writeSInt32(fieldNumber, value);
                break;
            case DEFAULT:
            case FIXED:
                writer.writeFixed16(fieldNumber, value);
                break;

            default:
                throw new SerializationException(
                        "IntEncodeType mismatch: " + serField.getIntEncodeType());
        }
    }

    private void writeInt32(CodedWriter writer, SerField serField, int value) {
        int fieldNumber = serField.getNumber();
        switch (serField.getIntEncodeType()) {
            case DEFAULT:
            case VARINT:
                writer.writeInt32(fieldNumber, value);
                break;
            case SIGNED_VARINT:
                writer.writeSInt32(fieldNumber, value);
                break;
            case FIXED:
                writer.writeFixed32(fieldNumber, value);
                break;

            default:
                throw new SerializationException(
                        "IntEncodeType mismatch: " + serField.getIntEncodeType());
        }
    }

    private void writeInt64(CodedWriter writer, SerField serField, long value) {
        int fieldNumber = serField.getNumber();
        switch (serField.getIntEncodeType()) {
            case DEFAULT:
            case VARINT:
                writer.writeInt64(fieldNumber, value);
                break;
            case SIGNED_VARINT:
                writer.writeSInt64(fieldNumber, value);
                break;
            case FIXED:
                writer.writeFixed64(fieldNumber, value);
                break;

            default:
                throw new SerializationException(
                        "IntEncodeType mismatch: " + serField.getIntEncodeType());
        }
    }

    /* ########## write sub ########## */

    private void writeSubValue(SerContext ctx, SerField serField, FieldType type, Object value) {
        if (value == null) {
            throw new SerializationException("value is null");
        }
        switch (type.getJavaType()) {
            case BYTE:
                writeByte(ctx, type, ((Byte) value).byteValue());
                break;

            case CHAR:
                writeChar(ctx, type, ((Character) value).charValue());
                break;

            case SHORT:
                writeShort(ctx, type, ((Short) value).shortValue());
                break;

            case INT:
                writeInt(ctx, type, ((Integer) value).intValue());
                break;

            case LONG:
                writeLong(ctx, type, ((Long) value).longValue());
                break;

            case FLOAT:
                writeFloat(ctx, type, ((Float) value).floatValue());
                break;

            case DOUBLE:
                writeDouble(ctx, type, ((Double) value).doubleValue());
                break;

            case BOOL:
                writeBoolean(ctx, type, ((Boolean) value).booleanValue());
                break;

            case INT_ENUM:
                writeIntEnum(ctx, type, (IntEnum) value);
                break;

            case ENUM:
                writeEnum(ctx, type, (Enum<?>) value);
                break;

            case BYTES:
                writeBytes(ctx, type, (byte[]) value);
                break;

            case STRING:
                writeString(ctx, type, (String) value);
                break;

            case LIST:
            case SET:
                writeCollection(ctx, serField, type, (Collection<?>) value);
                break;

            case INT_SET:
                writeIntSet(ctx, serField, type, (IntSet) value);
                break;

            case LONG_SET:
                writeLongSet(ctx, serField, type, (LongSet) value);
                break;

            case MAP:
                writeMap(ctx, serField, type, (Map<?, ?>) value);
                break;

            case INT_MAP:
                writeIntMap(ctx, serField, type, (IntMap<?>) value);
                break;

            case LONG_MAP:
                writeLongMap(ctx, serField, type, (LongMap<?>) value);
                break;

            case INT_VALUE_MAP:
                writeIntValueMap(ctx, serField, type, (IntValueMap<?>) value);
                break;

            case LONG_VALUE_MAP:
                writeLongValueMap(ctx, serField, type, (LongValueMap<?>) value);
                break;

            case FLOAT_VALUE_MAP:
                writeFloatValueMap(ctx, serField, type, (FloatValueMap<?>) value);
                break;

            case DOUBLE_VALUE_MAP:
                writeDoubleValueMap(ctx, serField, type, (DoubleValueMap<?>) value);
                break;

            case SER_OBJECT:
                writeSerObject(ctx, serField, type, value);
                break;

            default:
                throw new SerializationException("Unsupported type: " + type);
        }
    }

    private void writeByte(SerContext ctx, FieldType type, byte value) {
        CodedWriter writer = ctx.getWriter();
        writer.writeInt32NoTag(value);
    }

    private void writeChar(SerContext ctx, FieldType type, char value) {
        CodedWriter writer = ctx.getWriter();
        writer.writeInt32NoTag(value);
    }

    private void writeShort(SerContext ctx, FieldType type, short value) {
        CodedWriter writer = ctx.getWriter();
        writer.writeInt32NoTag(value);
    }

    private void writeInt(SerContext ctx, FieldType type, int value) {
        CodedWriter writer = ctx.getWriter();
        writer.writeInt32NoTag(value);
    }

    private void writeLong(SerContext ctx, FieldType type, long value) {
        CodedWriter writer = ctx.getWriter();
        writer.writeInt64NoTag(value);
    }

    private void writeFloat(SerContext ctx, FieldType type, float value) {
        CodedWriter writer = ctx.getWriter();
        writer.writeFloatNoTag(value);
    }

    private void writeDouble(SerContext ctx, FieldType type, double value) {
        CodedWriter writer = ctx.getWriter();
        writer.writeDoubleNoTag(value);
    }

    private void writeBoolean(SerContext ctx, FieldType type, boolean value) {
        CodedWriter writer = ctx.getWriter();
        writer.writeBoolNoTag(value);
    }

    private void writeIntEnum(SerContext ctx, FieldType type, IntEnum value) {
        CodedWriter writer = ctx.getWriter();
        writer.writeInt32NoTag(value.getId()); // write id
    }

    private void writeEnum(SerContext ctx, FieldType type, Enum<?> value) {
        CodedWriter writer = ctx.getWriter();
        writer.writeInt32NoTag(EnumUtils.tag(value)); // write id
    }


    private void writeBytes(SerContext ctx, FieldType type, byte[] value) {
        CodedWriter writer = ctx.getWriter();
        writer.writeBytesNoTag(value);
    }

    private void writeString(SerContext ctx, FieldType type, String value) {
        CodedWriter writer = ctx.getWriter();
        writer.writeStringNoTag(value);
    }

    private void writeCollection(SerContext ctx, SerField serField, FieldType type,
                                 Collection<?> value) {
        CodedWriter writer = ctx.getWriter();

        FieldType subType = type.getSubTypes().get(0);

        writer.writeWireType(subType.getWireType()); // sub wireType
        writer.writeInt32NoTag(value.size()); // 数量

        for (Object obj : value) {
            writeSubValue(ctx, serField, subType, obj);
        }
    }

    private void writeIntSet(SerContext ctx, SerField serField, FieldType type, IntSet value) {
        CodedWriter writer = ctx.getWriter();

        writer.writeWireType(WireFormat.WIRETYPE_VARINT); // sub wireType
        writer.writeInt32NoTag(value.size()); // 数量

        IntIterator itr = value.iterator();
        while (itr.hasNext()) {
            int e = itr.next();
            writer.writeInt32NoTag(e);
        }
    }

    private void writeLongSet(SerContext ctx, SerField serField, FieldType type, LongSet value) {
        CodedWriter writer = ctx.getWriter();

        writer.writeWireType(WireFormat.WIRETYPE_VARINT); // sub wireType
        writer.writeInt32NoTag(value.size()); // 数量

        LongIterator itr = value.iterator();
        while (itr.hasNext()) {
            long e = itr.next();
            writer.writeInt64NoTag(e);
        }
    }

    private void writeMap(SerContext ctx, SerField serField, FieldType type, Map<?, ?> value) {
        CodedWriter writer = ctx.getWriter();

        FieldType keyType = type.getSubTypes().get(0);
        FieldType valueType = type.getSubTypes().get(1);

        writer.writeWireType(keyType.getWireType()); // key wireType
        writer.writeWireType(valueType.getWireType()); // value wireType
        writer.writeInt32NoTag(value.size()); // 数量

        for (Map.Entry<?, ?> e : value.entrySet()) {
            writeSubValue(ctx, serField, keyType, e.getKey());
            writeSubValue(ctx, serField, valueType, e.getValue());
        }
    }

    private void writeIntMap(SerContext ctx, SerField serField, FieldType type, IntMap<?> value) {
        CodedWriter writer = ctx.getWriter();

        FieldType valueType = type.getSubTypes().get(0);

        writer.writeWireType(WireFormat.WIRETYPE_VARINT); // key wireType
        writer.writeWireType(valueType.getWireType()); // value wireType
        writer.writeInt32NoTag(value.size()); // 数量

        for (IntMap.Entry<?> e : value.entrySet()) {
            writer.writeInt32NoTag(e.getKey());
            writeSubValue(ctx, serField, valueType, e.getValue());
        }
    }

    private void writeLongMap(SerContext ctx, SerField serField, FieldType type, LongMap<?> value) {
        CodedWriter writer = ctx.getWriter();

        FieldType valueType = type.getSubTypes().get(0);

        writer.writeWireType(WireFormat.WIRETYPE_VARINT); // key wireType
        writer.writeWireType(valueType.getWireType()); // value wireType
        writer.writeInt32NoTag(value.size()); // 数量

        for (LongMap.Entry<?> e : value.entrySet()) {
            writer.writeInt64NoTag(e.getKey());
            writeSubValue(ctx, serField, valueType, e.getValue());
        }
    }

    private void writeIntValueMap(SerContext ctx, SerField serField, FieldType type,
                                  IntValueMap<?> value) {
        CodedWriter writer = ctx.getWriter();

        FieldType keyType = type.getSubTypes().get(0);

        writer.writeWireType(keyType.getWireType()); // key wireType
        writer.writeWireType(WireFormat.WIRETYPE_VARINT); // value wireType
        writer.writeInt32NoTag(value.size()); // 数量

        for (IntValueMap.Entry<?> e : value.entrySet()) {
            writeSubValue(ctx, serField, keyType, e.getKey());
            writer.writeInt32NoTag(e.getValue());
        }
    }

    private void writeLongValueMap(SerContext ctx, SerField serField, FieldType type,
                                   LongValueMap<?> value) {
        CodedWriter writer = ctx.getWriter();

        FieldType keyType = type.getSubTypes().get(0);

        writer.writeWireType(keyType.getWireType()); // key wireType
        writer.writeWireType(WireFormat.WIRETYPE_VARINT); // value wireType
        writer.writeInt32NoTag(value.size()); // 数量

        for (LongValueMap.Entry<?> e : value.entrySet()) {
            writeSubValue(ctx, serField, keyType, e.getKey());
            writer.writeInt64NoTag(e.getValue());
        }
    }

    private void writeFloatValueMap(SerContext ctx, SerField serField, FieldType type,
                                    FloatValueMap<?> value) {
        CodedWriter writer = ctx.getWriter();

        FieldType keyType = type.getSubTypes().get(0);

        writer.writeWireType(keyType.getWireType()); // key wireType
        writer.writeWireType(WireFormat.WIRETYPE_FIXED32); // value wireType
        writer.writeInt32NoTag(value.size()); // 数量

        for (FloatValueMap.Entry<?> e : value.entrySet()) {
            writeSubValue(ctx, serField, keyType, e.getKey());
            writer.writeFloatNoTag(e.getValue());
        }
    }

    private void writeDoubleValueMap(SerContext ctx, SerField serField, FieldType type,
                                     DoubleValueMap<?> value) {
        CodedWriter writer = ctx.getWriter();

        FieldType keyType = type.getSubTypes().get(0);

        writer.writeWireType(keyType.getWireType()); // key wireType
        writer.writeWireType(WireFormat.WIRETYPE_FIXED64); // value wireType
        writer.writeInt32NoTag(value.size()); // 数量

        for (DoubleValueMap.Entry<?> e : value.entrySet()) {
            writeSubValue(ctx, serField, keyType, e.getKey());
            writer.writeDoubleNoTag(e.getValue());
        }
    }

    private void writeSerObject(SerContext ctx, SerField serField, FieldType type, Object value) {
        SerClass valueSerClass = SerClassParser.ins().parse(value.getClass());

        writeSerObject(ctx, value, valueSerClass);
    }

}
