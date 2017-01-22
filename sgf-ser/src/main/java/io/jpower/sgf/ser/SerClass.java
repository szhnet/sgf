package io.jpower.sgf.ser;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import io.jpower.sgf.collection.IntMap;

/**
 * 类的序列化相关信息
 *
 * @author zheng.sun
 */
class SerClass {

    /**
     * 类
     */
    private Class<?> clazz;

    /**
     * 默认构造方法
     */
    private Constructor<?> constructor;

    /**
     * 需要进行序列化的字段
     */
    private List<SerField> fields;

    /**
     * 需要进行序列化的字段，key: fieldNumber
     */
    private IntMap<SerField> fieldMap;

    /**
     * 序列化前需要调用的方法
     */
    private Method beforeSerMethod;

    /**
     * 序列化后需要调用的方法
     */
    private Method afterSerMethod;

    /**
     * 反序列化前需要调用的方法
     */
    private Method beforeDeserMethod;

    /**
     * 反序列化后需要调用的方法
     */
    private Method afterDeserMethod;

    public SerClass(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    public List<SerField> getFields() {
        return fields;
    }

    public void setFields(List<SerField> fields) {
        this.fields = fields;
    }

    public SerField getField(int fieldNumber) {
        return fieldMap.get(fieldNumber);
    }

    public IntMap<SerField> getFieldMap() {
        return fieldMap;
    }

    public void setFieldMap(IntMap<SerField> fieldMap) {
        this.fieldMap = fieldMap;
    }

    public Method getBeforeSerMethod() {
        return beforeSerMethod;
    }

    public void setBeforeSerMethod(Method beforeSerMethod) {
        this.beforeSerMethod = beforeSerMethod;
    }

    public Method getAfterSerMethod() {
        return afterSerMethod;
    }

    public void setAfterSerMethod(Method afterSerMethod) {
        this.afterSerMethod = afterSerMethod;
    }

    public Method getBeforeDeserMethod() {
        return beforeDeserMethod;
    }

    public void setBeforeDeserMethod(Method beforeDeserMethod) {
        this.beforeDeserMethod = beforeDeserMethod;
    }

    public Method getAfterDeserMethod() {
        return afterDeserMethod;
    }

    public void setAfterDeserMethod(Method afterDeserMethod) {
        this.afterDeserMethod = afterDeserMethod;
    }

    @Override
    public String toString() {
        return "SerClass [clazz=" + clazz + "]";
    }

}
