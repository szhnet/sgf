package io.jpower.sgf.ser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 字段的序列化相关信息
 *
 * @author zheng.sun
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

    public SerField(Field field) {
        this.field = field;
    }

    public Field getField() {
        return field;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public FieldType getType() {
        return type;
    }

    public void setType(FieldType type) {
        this.type = type;
    }

    public Method getGetter() {
        return getter;
    }

    public void setGetter(Method getter) {
        this.getter = getter;
    }

    public Method getSetter() {
        return setter;
    }

    public void setSetter(Method setter) {
        this.setter = setter;
    }

    public boolean isIntern() {
        return intern;
    }

    public void setIntern(boolean intern) {
        this.intern = intern;
    }

    public IntEncodeType getIntEncodeType() {
        return intEncodeType;
    }

    public void setIntEncodeType(IntEncodeType intEncodeType) {
        this.intEncodeType = intEncodeType;
    }

    public Class<?> getDeSerClazz() {
        return deSerClazz;
    }

    public void setDeSerClazz(Class<?> deSerClazz) {
        this.deSerClazz = deSerClazz;
    }

    @Override
    public String toString() {
        return "SerField [number=" + number + ", type=" + type + ", field=" + field + ", getter="
                + getter + ", setter=" + setter + ", intern=" + intern + ", intEncodeType="
                + intEncodeType + ", deSerClazz=" + deSerClazz + "]";
    }

}
