package io.jpower.sgf.ser;

import java.util.List;

/**
 * 字段类型
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
class FieldType {

    /**
     * 原始类型
     */
    private Class<?> rawType;

    /**
     * java类型
     */
    private JavaType javaType;

    /**
     * 是否基本类型
     */
    private boolean primitive;

    /**
     * 编码格式 {@link WireFormat}
     */
    private int wireType;

    /**
     * 子类型，用来表示list和map的子类型
     */
    private List<FieldType> subTypes;

    FieldType(Class<?> clazz, JavaType javaType, int wireType) {
        this(clazz, javaType, false, wireType);
    }

    FieldType(Class<?> clazz, JavaType javaType, boolean primitive, int wireType) {
        this.rawType = clazz;
        this.javaType = javaType;
        this.primitive = primitive;
        this.wireType = wireType;
    }

    Class<?> getRawType() {
        return rawType;
    }

    public void setRawType(Class<?> rawType) {
        this.rawType = rawType;
    }

    JavaType getJavaType() {
        return javaType;
    }

    public void setJavaType(JavaType javaType) {
        this.javaType = javaType;
    }

    boolean isPrimitive() {
        return primitive;
    }

    void setPrimitive(boolean primitive) {
        this.primitive = primitive;
    }

    int getWireType() {
        return wireType;
    }

    void setWireType(int wireType) {
        this.wireType = wireType;
    }

    List<FieldType> getSubTypes() {
        return subTypes;
    }

    void setSubTypes(List<FieldType> subTypes) {
        this.subTypes = subTypes;
    }

    @Override
    public String toString() {
        return "FieldType [rawType=" + rawType + ", javaType=" + javaType + ", primitive="
                + primitive + ", wireType=" + wireType + ", subTypes=" + subTypes + "]";
    }

}
