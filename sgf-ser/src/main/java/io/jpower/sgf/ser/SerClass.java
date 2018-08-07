package io.jpower.sgf.ser;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import io.jpower.sgf.collection.IntMap;

/**
 * 类的序列化相关信息
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
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

    SerClass(Class<?> clazz) {
        this.clazz = clazz;
    }

    Class<?> getClazz() {
        return clazz;
    }

    Constructor<?> getConstructor() {
        return constructor;
    }

    void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    List<SerField> getFields() {
        return fields;
    }

    void setFields(List<SerField> fields) {
        this.fields = fields;
    }

    SerField getField(int fieldNumber) {
        return fieldMap.get(fieldNumber);
    }

    IntMap<SerField> getFieldMap() {
        return fieldMap;
    }

    void setFieldMap(IntMap<SerField> fieldMap) {
        this.fieldMap = fieldMap;
    }

    Method getBeforeSerMethod() {
        return beforeSerMethod;
    }

    void setBeforeSerMethod(Method beforeSerMethod) {
        this.beforeSerMethod = beforeSerMethod;
    }

    Method getAfterSerMethod() {
        return afterSerMethod;
    }

    void setAfterSerMethod(Method afterSerMethod) {
        this.afterSerMethod = afterSerMethod;
    }

    Method getBeforeDeserMethod() {
        return beforeDeserMethod;
    }

    void setBeforeDeserMethod(Method beforeDeserMethod) {
        this.beforeDeserMethod = beforeDeserMethod;
    }

    Method getAfterDeserMethod() {
        return afterDeserMethod;
    }

    void setAfterDeserMethod(Method afterDeserMethod) {
        this.afterDeserMethod = afterDeserMethod;
    }

    @Override
    public String toString() {
        return "SerClass [clazz=" + clazz + "]";
    }

}
