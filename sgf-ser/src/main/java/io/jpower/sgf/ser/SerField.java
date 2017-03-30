package io.jpower.sgf.ser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 字段的序列化相关信息
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
class SerField {

    /**
     * field number
     */
    private int number;

    /**
     * 类型
     */
    private FieldType type;

    /**
     * Filed
     */
    private Field field;

    /**
     * getter方法，如果没有为null
     */
    private Method getter;

    /**
     * setter方法，如果没有为null
     */
    private Method setter;

    /**
     * 用来标识该字段需要调用相应的intern方法，只能标记在字符串上
     */
    private boolean intern;

    /**
     * 整数的二进制编码方式
     */
    private IntEncodeType intEncodeType;

    /**
     * 反序列化时，指定具体类型
     */
    private Class<?> deSerClazz;

    SerField(Field field) {
        this.field = field;
    }

    Field getField() {
        return field;
    }

    int getNumber() {
        return number;
    }

    void setNumber(int number) {
        this.number = number;
    }

    FieldType getType() {
        return type;
    }

    void setType(FieldType type) {
        this.type = type;
    }

    Method getGetter() {
        return getter;
    }

    void setGetter(Method getter) {
        this.getter = getter;
    }

    Method getSetter() {
        return setter;
    }

    void setSetter(Method setter) {
        this.setter = setter;
    }

    boolean isIntern() {
        return intern;
    }

    void setIntern(boolean intern) {
        this.intern = intern;
    }

    IntEncodeType getIntEncodeType() {
        return intEncodeType;
    }

    void setIntEncodeType(IntEncodeType intEncodeType) {
        this.intEncodeType = intEncodeType;
    }

    Class<?> getDeSerClazz() {
        return deSerClazz;
    }

    void setDeSerClazz(Class<?> deSerClazz) {
        this.deSerClazz = deSerClazz;
    }

    @Override
    public String toString() {
        return "SerField [number=" + number + ", type=" + type + ", field=" + field + ", getter="
                + getter + ", setter=" + setter + ", intern=" + intern + ", intEncodeType="
                + intEncodeType + ", deSerClazz=" + deSerClazz + "]";
    }

}
