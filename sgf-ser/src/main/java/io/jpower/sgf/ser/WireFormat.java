package io.jpower.sgf.ser;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
final class WireFormat {

    private WireFormat() {

    }

    /**
     * byte, char, short, int, long, bool, IntEnum
     */
    static final int WIRETYPE_VARINT = 0;

    /**
     * fixed byte
     */
    static final int WIRETYPE_FIXED8 = 1;

    /**
     * fixed short, fixed char
     */
    static final int WIRETYPE_FIXED16 = 2;

    /**
     * fixed int, float
     */
    static final int WIRETYPE_FIXED32 = 3;

    /**
     * fixed long, double
     */
    static final int WIRETYPE_FIXED64 = 4;

    /**
     * byte array, string
     */
    static final int WIRETYPE_BYTES = 5;

    /**
     * list, set
     */
    static final int WIRETYPE_COLLECTION = 6;

    /**
     * map
     */
    static final int WIRETYPE_MAP = 7;

    /**
     * serializable object
     */
    static final int WIRETYPE_SER_OBJECT = 8;

    static final int TAG_TYPE_BITS = 4;

    static final int TAG_TYPE_MASK = (1 << TAG_TYPE_BITS) - 1;

    final static int MAX_FIELD_NUMBER = (1 << 28) - 1;

    /**
     * Given a tag value, determines the wire type (the lower 4 bits).
     */
    static int getTagWireType(final int tag) {
        return tag & TAG_TYPE_MASK;
    }

    /**
     * Given a tag value, determines the field number (the upper 28 bits).
     */
    static int getTagFieldNumber(final int tag) {
        return tag >>> TAG_TYPE_BITS;
    }

    /**
     * Makes a tag value given a field number and wire type.
     */
    static int makeTag(final int fieldNumber, final int wireType) {
        return (fieldNumber << TAG_TYPE_BITS) | wireType;
    }

}
