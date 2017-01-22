package io.jpower.sgf.ser;

import java.util.List;

/**
 * 字段类型
 *
 * @author zheng.sun
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

    public FieldType(Class<?> clazz, JavaType javaType, int wireType) {
        this(clazz, javaType, false, wireType);
    }

    public FieldType(Class<?> clazz, JavaType javaType, boolean primitive, int wireType) {
        this.rawType = clazz;
        this.javaType = javaType;
        this.primitive = primitive;
        this.wireType = wireType;
    }

    public Class<?> getRawType() {
        return rawType;
    }

    public void setRawType(Class<?> rawType) {
        this.rawType = rawType;
    }

    public JavaType getJavaType() {
        return javaType;
    }

    public void setJavaType(JavaType javaType) {
        this.javaType = javaType;
    }

    public boolean isPrimitive() {
        return primitive;
    }

    public void setPrimitive(boolean primitive) {
        this.primitive = primitive;
    }

    public int getWireType() {
        return wireType;
    }

    public void setWireType(int wireType) {
        this.wireType = wireType;
    }

    public List<FieldType> getSubTypes() {
        return subTypes;
    }

    public void setSubTypes(List<FieldType> subTypes) {
        this.subTypes = subTypes;
    }

    @Override
    public String toString() {
        return "FieldType [rawType=" + rawType + ", javaType=" + javaType + ", primitive="
                + primitive + ", wireType=" + wireType + ", subTypes=" + subTypes + "]";
    }

}
