package io.jpower.sgf.ser;

import java.io.UnsupportedEncodingException;

import io.jpower.sgf.utils.JavaUtils;

/**
 * 包装了一个字节数组
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
class ByteArrayReader extends CodedReader {

    private byte buf[];

    private int pos;

    private int limit;

    ByteArrayReader(byte buf[]) {
        this.buf = buf;
        this.pos = 0;
        this.limit = buf.length;
    }

    ByteArrayReader(byte buf[], int offset, int length) {
        this.buf = buf;
        this.pos = offset;
        this.limit = Math.min(offset + length, buf.length);
    }

    /* ########## 实现父类方法 ########## */

    @Override
    String readString(int size) {
        checkSize(size);

        final String result = new String(buf, pos, size, Utils.CHARSET);
        pos += size;

        return result;
    }

    @Override
    byte readRawByte() {
        return read();
    }

    @Override
    void readRawBytes(byte[] value, int offset, int len) {
        read(value, offset, len);
    }

    @Override
    void skipRawBytes(int size) {
        skip(size);
    }

    /* ########## 自己的方法 ########## */

    byte read() {
        if (pos >= limit) {
            throw new IndexOutOfBoundsException("Not enough readable bytes.");
        }

        return buf[pos++];
    }

    void read(byte b[], int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }

        int avail = limit - pos;
        if (len > avail) {
            throw new IndexOutOfBoundsException("Not enough readable bytes.");
        }
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
    }

    void skip(int len) {
        checkSize(len);

        pos += len;
    }

    int available() {
        return limit - pos;
    }

    void checkSize(int size) {
        if (size < 0) {
            throw new SerializationException("Negative size: " + size);
        }
        if (size > limit - pos) {
            throw new IndexOutOfBoundsException("Not enough readable bytes.");
        }
    }

}
