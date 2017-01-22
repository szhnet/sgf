package io.jpower.sgf.ser;

import java.io.UnsupportedEncodingException;

import io.jpower.sgf.utils.JavaUtils;

/**
 * 用来进行解码
 * <p>
 * <ul>
 * <li>参考了
 * <a href="https://developers.google.com/protocol-buffers/docs/encoding">
 * protobuf</a>的编码</li>
 * </ul>
 *
 * @author zheng.sun
 */
abstract class CodedReader {

    /* ########## read value ########## */

    /**
     * Read an {@code int32} field value from the stream.
     */
    public int readInt32() {
        return readRawVarint32();
    }

    /**
     * Read an {@code int64} field value from the stream.
     */
    public long readInt64() {
        return readRawVarint64();
    }

    /**
     * Read an {@code sint32} field value from the stream.
     */
    public int readSInt32() {
        return decodeZigZag32(readRawVarint32());
    }

    /**
     * Read an {@code sint64} field value from the stream.
     */
    public long readSInt64() {
        return decodeZigZag64(readRawVarint64());
    }

    /**
     * Read a {@code fixed8} field value from the stream.
     */
    public byte readFixed8() {
        return readRawLittleEndian8();
    }

    /**
     * Read a {@code fixed16} field value from the stream.
     */
    public short readFixed16() {
        return readRawLittleEndian16();
    }

    /**
     * Read a {@code fixed32} field value from the stream.
     */
    public int readFixed32() {
        return readRawLittleEndian32();
    }

    /**
     * Read a {@code fixed64} field value from the stream.
     */
    public long readFixed64() {
        return readRawLittleEndian64();
    }

    /**
     * Read a {@code float} field value from the stream.
     */
    public float readFloat() {
        return Float.intBitsToFloat(readRawLittleEndian32());
    }

    /**
     * Read a {@code double} field value from the stream.
     */
    public double readDouble() {
        return Double.longBitsToDouble(readRawLittleEndian64());
    }

    /**
     * Read a {@code bool} field value from the stream.
     */
    public boolean readBool() {
        return readRawVarint64() != 0;
    }

    /**
     * Read a {@code string} field value from the stream.
     */
    public String readString(int size) {
        if (size < 0) {
            throw SerializationException.negativeSize(size);
        }

        if (size == 0) {
            return "";
        }

        byte[] bytes = readBytes(size);
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw JavaUtils.sneakyThrow(e);
        }
    }

    public byte[] readBytes(int size) {
        if (size == 0) {
            return Utils.EMPTY_BYTES;
        }

        byte[] buf = new byte[size];
        readRawBytes(buf);
        return buf;
    }

    public int readTag() {
        return readRawVarint32();
    }

    public int readWireType() {
        return readRawVarint32();
    }

    /* ########## read raw value ########## */

    public abstract byte readRawByte();

    public void readRawBytes(final byte[] value) {
        readRawBytes(value, 0, value.length);
    }

    public abstract void readRawBytes(final byte[] value, int offset, int len);

    public int readRawVarint32() {
        int result = 0;
        int shift = 0;
        for (; shift < 29; shift += 7) {
            final byte b = readRawByte();
            result |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
        }
        long longResult = result;
        for (; shift < 64; shift += 7) {
            final byte b = readRawByte();
            longResult |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return (int) longResult;
            }
        }
        throw SerializationException.malformedVarint();
    }

    public long readRawVarint64() {
        long result = 0;
        for (int shift = 0; shift < 64; shift += 7) {
            final byte b = readRawByte();
            result |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
        }
        throw SerializationException.malformedVarint();
    }

    /**
     * Read a 8-bit little-endian integer from the stream.
     */
    public byte readRawLittleEndian8() {
        return readRawByte();
    }

    /**
     * Read a 16-bit little-endian integer from the stream.
     */
    public short readRawLittleEndian16() {
        return (short) (((readRawByte() & 0xff)) | ((readRawByte() & 0xff) << 8));
    }

    /**
     * Read a 32-bit little-endian integer from the stream.
     */
    public int readRawLittleEndian32() {
        return (((readRawByte() & 0xff)) |
                ((readRawByte() & 0xff) << 8) |
                ((readRawByte() & 0xff) << 16) |
                ((readRawByte() & 0xff) << 24));
    }

    /**
     * Read a 64-bit little-endian integer from the stream.
     */
    public long readRawLittleEndian64() {
        return ((((long) readRawByte() & 0xffL)) |
                (((long) readRawByte() & 0xffL) << 8) |
                (((long) readRawByte() & 0xffL) << 16) |
                (((long) readRawByte() & 0xffL) << 24) |
                (((long) readRawByte() & 0xffL) << 32) |
                (((long) readRawByte() & 0xffL) << 40) |
                (((long) readRawByte() & 0xffL) << 48) |
                (((long) readRawByte() & 0xffL) << 56));
    }

    public void skipRawVarint() {
        for (int i = 0; i < 10; i++) { // varint最多10个字节
            if (readRawByte() >= 0) {
                return;
            }
        }
        throw SerializationException.malformedVarint();
    }

    public void skipRawBytes(final int size) {
        if (size < 0) {
            throw SerializationException.negativeSize(size);
        }
        for (int i = 0; i < size; i++) {
            readRawByte();
        }
    }

    /* ########## other ########## */

    /**
     * Decode a ZigZag-encoded 32-bit value. ZigZag encodes signed integers into
     * values that can be efficiently encoded with varint. (Otherwise, negative
     * values must be sign-extended to 64 bits to be varint encoded, thus always
     * taking 10 bytes on the wire.)
     *
     * @param n An unsigned 32-bit integer, stored in a signed int because
     *          Java has no explicit unsigned support.
     * @return A signed 32-bit integer.
     */
    public static int decodeZigZag32(final int n) {
        return (n >>> 1) ^ -(n & 1);
    }

    /**
     * Decode a ZigZag-encoded 64-bit value. ZigZag encodes signed integers into
     * values that can be efficiently encoded with varint. (Otherwise, negative
     * values must be sign-extended to 64 bits to be varint encoded, thus always
     * taking 10 bytes on the wire.)
     *
     * @param n An unsigned 64-bit integer, stored in a signed int because
     *          Java has no explicit unsigned support.
     * @return A signed 64-bit integer.
     */
    public static long decodeZigZag64(final long n) {
        return (n >>> 1) ^ -(n & 1);
    }

}
