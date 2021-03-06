/**
 * Autogenerated by Thrift Compiler (0.8.0)
 * <p>
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *
 * @generated
 */
package io.jpower.sgf.net.msg;

import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;
import org.apache.thrift.scheme.TupleScheme;

/**
 * 请求登录返回
 *
 * @Message(2)
 */
@SuppressWarnings("all")
public class GcMockLoginInfo implements org.apache.thrift.TBase<GcMockLoginInfo, GcMockLoginInfo._Fields>, java.io.Serializable, Cloneable {
    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("GcMockLoginInfo");

    private static final org.apache.thrift.protocol.TField SERVER_TIME_FIELD_DESC = new org.apache.thrift.protocol.TField("serverTime", org.apache.thrift.protocol.TType.DOUBLE, (short) 1);
    private static final org.apache.thrift.protocol.TField IS_NEW_USER_FIELD_DESC = new org.apache.thrift.protocol.TField("isNewUser", org.apache.thrift.protocol.TType.BOOL, (short) 2);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new GcMockLoginInfoStandardSchemeFactory());
        schemes.put(TupleScheme.class, new GcMockLoginInfoTupleSchemeFactory());
    }

    public double serverTime; // required
    public boolean isNewUser; // required

    /**
     * The set of fields this struct contains, along with convenience methods for finding and manipulating them.
     */
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {
        SERVER_TIME((short) 1, "serverTime"),
        IS_NEW_USER((short) 2, "isNewUser");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        /**
         * Find the _Fields constant that matches fieldId, or null if its not found.
         */
        public static _Fields findByThriftId(int fieldId) {
            switch (fieldId) {
                case 1: // SERVER_TIME
                    return SERVER_TIME;
                case 2: // IS_NEW_USER
                    return IS_NEW_USER;
                default:
                    return null;
            }
        }

        /**
         * Find the _Fields constant that matches fieldId, throwing an exception
         * if it is not found.
         */
        public static _Fields findByThriftIdOrThrow(int fieldId) {
            _Fields fields = findByThriftId(fieldId);
            if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
            return fields;
        }

        /**
         * Find the _Fields constant that matches name, or null if its not found.
         */
        public static _Fields findByName(String name) {
            return byName.get(name);
        }

        private final short _thriftId;
        private final String _fieldName;

        _Fields(short thriftId, String fieldName) {
            _thriftId = thriftId;
            _fieldName = fieldName;
        }

        public short getThriftFieldId() {
            return _thriftId;
        }

        public String getFieldName() {
            return _fieldName;
        }
    }

    // isset id assignments
    private static final int __SERVERTIME_ISSET_ID = 0;
    private static final int __ISNEWUSER_ISSET_ID = 1;
    private BitSet __isset_bit_vector = new BitSet(2);
    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.SERVER_TIME, new org.apache.thrift.meta_data.FieldMetaData("serverTime", org.apache.thrift.TFieldRequirementType.REQUIRED,
                new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.IS_NEW_USER, new org.apache.thrift.meta_data.FieldMetaData("isNewUser", org.apache.thrift.TFieldRequirementType.REQUIRED,
                new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(GcMockLoginInfo.class, metaDataMap);
    }

    public GcMockLoginInfo() {
    }

    public GcMockLoginInfo(
            double serverTime,
            boolean isNewUser) {
        this();
        this.serverTime = serverTime;
        setServerTimeIsSet(true);
        this.isNewUser = isNewUser;
        setIsNewUserIsSet(true);
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public GcMockLoginInfo(GcMockLoginInfo other) {
        __isset_bit_vector.clear();
        __isset_bit_vector.or(other.__isset_bit_vector);
        this.serverTime = other.serverTime;
        this.isNewUser = other.isNewUser;
    }

    public GcMockLoginInfo deepCopy() {
        return new GcMockLoginInfo(this);
    }

    @Override
    public void clear() {
        setServerTimeIsSet(false);
        this.serverTime = 0.0;
        setIsNewUserIsSet(false);
        this.isNewUser = false;
    }

    public double getServerTime() {
        return this.serverTime;
    }

    public GcMockLoginInfo setServerTime(double serverTime) {
        this.serverTime = serverTime;
        setServerTimeIsSet(true);
        return this;
    }

    public void unsetServerTime() {
        __isset_bit_vector.clear(__SERVERTIME_ISSET_ID);
    }

    /**
     * Returns true if field serverTime is set (has been assigned a value) and false otherwise
     */
    public boolean isSetServerTime() {
        return __isset_bit_vector.get(__SERVERTIME_ISSET_ID);
    }

    public void setServerTimeIsSet(boolean value) {
        __isset_bit_vector.set(__SERVERTIME_ISSET_ID, value);
    }

    public boolean isIsNewUser() {
        return this.isNewUser;
    }

    public GcMockLoginInfo setIsNewUser(boolean isNewUser) {
        this.isNewUser = isNewUser;
        setIsNewUserIsSet(true);
        return this;
    }

    public void unsetIsNewUser() {
        __isset_bit_vector.clear(__ISNEWUSER_ISSET_ID);
    }

    /**
     * Returns true if field isNewUser is set (has been assigned a value) and false otherwise
     */
    public boolean isSetIsNewUser() {
        return __isset_bit_vector.get(__ISNEWUSER_ISSET_ID);
    }

    public void setIsNewUserIsSet(boolean value) {
        __isset_bit_vector.set(__ISNEWUSER_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, Object value) {
        switch (field) {
            case SERVER_TIME:
                if (value == null) {
                    unsetServerTime();
                } else {
                    setServerTime((Double) value);
                }
                break;

            case IS_NEW_USER:
                if (value == null) {
                    unsetIsNewUser();
                } else {
                    setIsNewUser((Boolean) value);
                }
                break;

        }
    }

    public Object getFieldValue(_Fields field) {
        switch (field) {
            case SERVER_TIME:
                return Double.valueOf(getServerTime());

            case IS_NEW_USER:
                return Boolean.valueOf(isIsNewUser());

        }
        throw new IllegalStateException();
    }

    /**
     * Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise
     */
    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }

        switch (field) {
            case SERVER_TIME:
                return isSetServerTime();
            case IS_NEW_USER:
                return isSetIsNewUser();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof GcMockLoginInfo)
            return this.equals((GcMockLoginInfo) that);
        return false;
    }

    public boolean equals(GcMockLoginInfo that) {
        if (that == null)
            return false;

        boolean this_present_serverTime = true;
        boolean that_present_serverTime = true;
        if (this_present_serverTime || that_present_serverTime) {
            if (!(this_present_serverTime && that_present_serverTime))
                return false;
            if (this.serverTime != that.serverTime)
                return false;
        }

        boolean this_present_isNewUser = true;
        boolean that_present_isNewUser = true;
        if (this_present_isNewUser || that_present_isNewUser) {
            if (!(this_present_isNewUser && that_present_isNewUser))
                return false;
            if (this.isNewUser != that.isNewUser)
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public int compareTo(GcMockLoginInfo other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }

        int lastComparison = 0;
        GcMockLoginInfo typedOther = (GcMockLoginInfo) other;

        lastComparison = Boolean.valueOf(isSetServerTime()).compareTo(typedOther.isSetServerTime());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetServerTime()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.serverTime, typedOther.serverTime);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetIsNewUser()).compareTo(typedOther.isSetIsNewUser());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetIsNewUser()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.isNewUser, typedOther.isNewUser);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        return 0;
    }

    public _Fields fieldForId(int fieldId) {
        return _Fields.findByThriftId(fieldId);
    }

    public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
        schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
        schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GcMockLoginInfo(");
        boolean first = true;

        sb.append("serverTime:");
        sb.append(this.serverTime);
        first = false;
        if (!first) sb.append(", ");
        sb.append("isNewUser:");
        sb.append(this.isNewUser);
        first = false;
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
        // check for required fields
        // alas, we cannot check 'serverTime' because it's a primitive and you chose the non-beans generator.
        // alas, we cannot check 'isNewUser' because it's a primitive and you chose the non-beans generator.
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        try {
            write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        try {
            // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
            __isset_bit_vector = new BitSet(1);
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class GcMockLoginInfoStandardSchemeFactory implements SchemeFactory {
        public GcMockLoginInfoStandardScheme getScheme() {
            return new GcMockLoginInfoStandardScheme();
        }
    }

    private static class GcMockLoginInfoStandardScheme extends StandardScheme<GcMockLoginInfo> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, GcMockLoginInfo struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch (schemeField.id) {
                    case 1: // SERVER_TIME
                        if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
                            struct.serverTime = iprot.readDouble();
                            struct.setServerTimeIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2: // IS_NEW_USER
                        if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
                            struct.isNewUser = iprot.readBool();
                            struct.setIsNewUserIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    default:
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                }
                iprot.readFieldEnd();
            }
            iprot.readStructEnd();

            // check for required fields of primitive type, which can't be checked in the validate method
            if (!struct.isSetServerTime()) {
                throw new org.apache.thrift.protocol.TProtocolException("Required field 'serverTime' was not found in serialized data! Struct: " + toString());
            }
            if (!struct.isSetIsNewUser()) {
                throw new org.apache.thrift.protocol.TProtocolException("Required field 'isNewUser' was not found in serialized data! Struct: " + toString());
            }
            struct.validate();
        }

        public void write(org.apache.thrift.protocol.TProtocol oprot, GcMockLoginInfo struct) throws org.apache.thrift.TException {
            struct.validate();

            oprot.writeStructBegin(STRUCT_DESC);
            oprot.writeFieldBegin(SERVER_TIME_FIELD_DESC);
            oprot.writeDouble(struct.serverTime);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(IS_NEW_USER_FIELD_DESC);
            oprot.writeBool(struct.isNewUser);
            oprot.writeFieldEnd();
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }

    }

    private static class GcMockLoginInfoTupleSchemeFactory implements SchemeFactory {
        public GcMockLoginInfoTupleScheme getScheme() {
            return new GcMockLoginInfoTupleScheme();
        }
    }

    private static class GcMockLoginInfoTupleScheme extends TupleScheme<GcMockLoginInfo> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, GcMockLoginInfo struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            oprot.writeDouble(struct.serverTime);
            oprot.writeBool(struct.isNewUser);
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, GcMockLoginInfo struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            struct.serverTime = iprot.readDouble();
            struct.setServerTimeIsSet(true);
            struct.isNewUser = iprot.readBool();
            struct.setIsNewUserIsSet(true);
        }
    }

}

