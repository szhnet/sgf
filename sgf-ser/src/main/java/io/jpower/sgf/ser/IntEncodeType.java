package io.jpower.sgf.ser;

/**
 * 整数的编码类型
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public enum IntEncodeType {

    /**
     * varint
     */
    VARINT(WireFormat.WIRETYPE_VARINT, WireFormat.WIRETYPE_VARINT, WireFormat.WIRETYPE_VARINT, WireFormat.WIRETYPE_VARINT),

    /**
     * 对有符号的整数进行varint编码时有优化
     */
    SIGNED_VARINT(WireFormat.WIRETYPE_VARINT, WireFormat.WIRETYPE_VARINT, WireFormat.WIRETYPE_VARINT, WireFormat.WIRETYPE_VARINT),

    /**
     * 固定字节数
     */
    FIXED(WireFormat.WIRETYPE_FIXED8, WireFormat.WIRETYPE_FIXED16, WireFormat.WIRETYPE_FIXED32, WireFormat.WIRETYPE_FIXED64),;

    private final int int8WireType;

    private final int int16WireType;

    private final int int32WireType;

    private final int int64WireType;

    IntEncodeType(int int8WireType, int int16WireType, int int32WireType,
                  int int64WireType) {
        this.int8WireType = int8WireType;
        this.int16WireType = int16WireType;
        this.int32WireType = int32WireType;
        this.int64WireType = int64WireType;
    }

    public int getInt8WireType() {
        return int8WireType;
    }

    public int getInt16WireType() {
        return int16WireType;
    }

    public int getInt32WireType() {
        return int32WireType;
    }

    public int getInt64WireType() {
        return int64WireType;
    }

}
