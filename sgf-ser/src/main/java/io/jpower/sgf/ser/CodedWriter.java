package io.jpower.sgf.ser;

import java.io.UnsupportedEncodingException;

import io.jpower.sgf.utils.JavaUtils;

/**
 * 用来进行编码
 * <p>
 * <ul>
 * <li>参考了
 * <a href="https://developers.google.com/protocol-buffers/docs/encoding">
 * protobuf</a>的编码</li>
 * </ul>
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
abstract class CodedWriter {

    CodedWriter() {

    }

    /* ########## write type value ########## */

    void writeStop() {
        writeTag(0, WireFormat.WIRETYPE_VARINT); // fieldNumber == 0
    }

    /**
     * Write an {@code int32} field, including tag, to the stream.
     */
    void writeInt32(final int fieldNumber, final int value) {
        writeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
        writeInt32NoTag(value);
    }

    /**
     * Write an {@code int64} field, including tag, to the stream.
     */
    void writeInt64(final int fieldNumber, final long value) {
        writeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
        writeInt64NoTag(value);
    }

    /**
     * Write an {@code sint32} field, including tag, to the stream.
     */
    void writeSInt32(final int fieldNumber, final int value) {
        writeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
        writeSInt32NoTag(value);
    }

    /**
     * Write an {@code sint64} field, including tag, to the stream.
     */
    void writeSInt64(final int fieldNumber, final long value) {
        writeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
        writeSInt64NoTag(value);
    }

    /**
     * Write a {@code fixed8} field, including tag, to the stream.
     */
    void writeFixed8(final int fieldNumber, final int value) {
        writeTag(fieldNumber, WireFormat.WIRETYPE_FIXED8);
        writeFixed8NoTag(value);
    }

    /**
     * Write a {@code fixed16} field, including tag, to the stream.
     */
    void writeFixed16(final int fieldNumber, final int value) {
        writeTag(fieldNumber, WireFormat.WIRETYPE_FIXED16);
        writeFixed16NoTag(value);
    }

    /**
     * Write a {@code fixed32} field, including tag, to the stream.
     */
    void writeFixed32(final int fieldNumber, final int value) {
        writeTag(fieldNumber, WireFormat.WIRETYPE_FIXED32);
        writeFixed32NoTag(value);
    }

    /**
     * Write a {@code fixed64} field, including tag, to the stream.
     */
    void writeFixed64(final int fieldNumber, final long value) {
        writeTag(fieldNumber, WireFormat.WIRETYPE_FIXED64);
        writeFixed64NoTag(value);
    }

    /**
     * Write a {@code float} field, including tag, to the stream.
     */
    void writeFloat(final int fieldNumber, final float value) {
        writeTag(fieldNumber, WireFormat.WIRETYPE_FIXED32);
        writeFloatNoTag(value);
    }

    /**
     * Write a {@code double} field, including tag, to the stream.
     */
    void writeDouble(final int fieldNumber, final double value) {
        writeTag(fieldNumber, WireFormat.WIRETYPE_FIXED64);
        writeDoubleNoTag(value);
    }

    /**
     * Write a {@code bool} field, including tag, to the stream.
     */
    void writeBool(final int fieldNumber, final boolean value) {
        writeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
        writeBoolNoTag(value);
    }

    /**
     * Write a {@code bytes} field, including tag, to the stream.
     */
    void writeBytes(final int fieldNumber, final byte[] value) {
        writeTag(fieldNumber, WireFormat.WIRETYPE_BYTES);
        writeBytesNoTag(value);
    }

    /**
     * Write a {@code string} field, including tag, to the stream.
     */
    void writeString(final int fieldNumber, final String value) {
        writeTag(fieldNumber, WireFormat.WIRETYPE_BYTES);
        writeStringNoTag(value);
    }

    /* ########## write value ########## */

    /**
     * Write an {@code int32} field to the stream.
     */
    void writeInt32NoTag(final int value) {
        if (value >= 0) {
            writeRawVarint32(value);
        } else {
            // Must sign-extend.
            writeRawVarint64(value); // 符号扩展，为了保证32位的varint和64位的varint兼容
        }
    }

    /**
     * Write an {@code int64} field to the stream.
     */
    void writeInt64NoTag(final long value) {
        writeRawVarint64(value);
    }

    /**
     * Write an {@code sint32} field to the stream.
     */
    void writeSInt32NoTag(final int value) {
        writeRawVarint32(encodeZigZag32(value));
    }

    /**
     * Write an {@code sint64} field to the stream.
     */
    void writeSInt64NoTag(final long value) {
        writeRawVarint64(encodeZigZag64(value));
    }

    /**
     * Write a {@code fixed8} field to the stream.
     */
    void writeFixed8NoTag(final int value) {
        writeRawLittleEndian8(value);
    }

    /**
     * Write a {@code fixed16} field to the stream.
     */
    void writeFixed16NoTag(final int value) {
        writeRawLittleEndian16(value);
    }

    /**
     * Write a {@code fixed32} field to the stream.
     */
    void writeFixed32NoTag(final int value) {
        writeRawLittleEndian32(value);
    }

    /**
     * Write a {@code fixed64} field to the stream.
     */
    void writeFixed64NoTag(final long value) {
        writeRawLittleEndian64(value);
    }

    /**
     * Write a {@code float} field to the stream.
     */
    void writeFloatNoTag(final float value) {
        writeRawLittleEndian32(Float.floatToRawIntBits(value));
    }

    /**
     * Write a {@code double} field to the stream.
     */
    void writeDoubleNoTag(final double value) {
        writeRawLittleEndian64(Double.doubleToRawLongBits(value));
    }

    /**
     * Write a {@code bool} field to the stream.
     */
    void writeBoolNoTag(final boolean value) {
        writeRawByte(value ? 1 : 0);
    }

    /**
     * Write a {@code bytes} field to the stream.
     */
    void writeBytesNoTag(final byte[] value) {
        writeRawVarint32(value.length);
        writeRawBytes(value);
    }

    /**
     * Write a {@code string} field to the stream.
     */
    void writeStringNoTag(final String value) {
        // Unfortunately there does not appear to be any way to tell Java to
        // encode
        // UTF-8 directly into our buffer, so we have to let it create its own
        // byte
        // array and then copy.
        byte[] bytes = value.getBytes(Utils.CHARSET);
        writeRawVarint32(bytes.length);
        writeRawBytes(bytes);
    }

    void writeTag(final int fieldNumber, final int wireType) {
        writeRawVarint32(WireFormat.makeTag(fieldNumber, wireType));
    }

    void writeWireType(final int wireType) {
        writeRawVarint32(wireType);
    }

    /* ########## write raw value ########## */

    /**
     * Write a single byte, represented by an integer value.
     */
    void writeRawByte(final int value) {
        writeRawByte((byte) value);
    }

    /**
     * Write a single byte.
     */
    abstract void writeRawByte(final byte value);

    /**
     * Write an array of bytes.
     */
    void writeRawBytes(final byte[] value) {
        writeRawBytes(value, 0, value.length);
    }

    abstract void writeRawBytes(final byte[] value, int offset, int len);

    /**
     * Encode and write a varint. {@code value} is treated as unsigned, so it
     * won't be sign-extended if negative.
     */
    void writeRawVarint32(int value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
                writeRawByte(value);
                return;
            } else {
                writeRawByte((value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }

    /**
     * Encode and write a varint.
     */
    void writeRawVarint64(long value) {
        while (true) {
            if ((value & ~0x7FL) == 0) {
                writeRawByte((int) value);
                return;
            } else {
                writeRawByte(((int) value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }

    /**
     * Write a little-endian 8-bit integer.
     */
    void writeRawLittleEndian8(final int value) {
        writeRawByte((value) & 0xFF);
    }

    /**
     * Write a little-endian 16-bit integer.
     */
    void writeRawLittleEndian16(final int value) {
        writeRawByte((value) & 0xFF);
        writeRawByte((value >> 8) & 0xFF);
    }

    /**
     * Write a little-endian 32-bit integer.
     */
    void writeRawLittleEndian32(final int value) {
        writeRawByte((value) & 0xFF);
        writeRawByte((value >> 8) & 0xFF);
        writeRawByte((value >> 16) & 0xFF);
        writeRawByte((value >> 24) & 0xFF);
    }

    /**
     * Write a little-endian 64-bit integer.
     */
    void writeRawLittleEndian64(final long value) {
        writeRawByte((int) (value) & 0xFF);
        writeRawByte((int) (value >> 8) & 0xFF);
        writeRawByte((int) (value >> 16) & 0xFF);
        writeRawByte((int) (value >> 24) & 0xFF);
        writeRawByte((int) (value >> 32) & 0xFF);
        writeRawByte((int) (value >> 40) & 0xFF);
        writeRawByte((int) (value >> 48) & 0xFF);
        writeRawByte((int) (value >> 56) & 0xFF);
    }

    /* ########## other ########## */

    /**
     * Encode a ZigZag-encoded 32-bit value. ZigZag encodes signed integers into
     * values that can be efficiently encoded with varint. (Otherwise, negative
     * values must be sign-extended to 64 bits to be varint encoded, thus always
     * taking 10 bytes on the wire.)
     *
     * @param n A signed 32-bit integer.
     * @return An unsigned 32-bit integer, stored in a signed int because Java
     * has no explicit unsigned support.
     */
    static int encodeZigZag32(final int n) {
        // Note: the right-shift must be arithmetic
        return (n << 1) ^ (n >> 31);
    }

    /**
     * Encode a ZigZag-encoded 64-bit value. ZigZag encodes signed integers into
     * values that can be efficiently encoded with varint. (Otherwise, negative
     * values must be sign-extended to 64 bits to be varint encoded, thus always
     * taking 10 bytes on the wire.)
     *
     * @param n A signed 64-bit integer.
     * @return An unsigned 64-bit integer, stored in a signed int because Java
     * has no explicit unsigned support.
     */
    static long encodeZigZag64(final long n) {
        // Note: the right-shift must be arithmetic
        return (n << 1) ^ (n >> 63);
    }

}
