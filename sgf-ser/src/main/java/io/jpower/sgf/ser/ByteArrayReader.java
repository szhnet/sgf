package io.jpower.sgf.ser;

import java.io.UnsupportedEncodingException;

import io.jpower.sgf.utils.JavaUtils;

/**
 * 包装了一个字节数组
 *
 * @author zheng.sun
 */
class ByteArrayReader extends CodedReader {

    private byte buf[];

    private int pos;

    private int limit;

    public ByteArrayReader(byte buf[]) {
        this.buf = buf;
        this.pos = 0;
        this.limit = buf.length;
    }

    public ByteArrayReader(byte buf[], int offset, int length) {
        this.buf = buf;
        this.pos = offset;
        this.limit = Math.min(offset + length, buf.length);
    }

    /* ########## 实现父类方法 ########## */

    @Override
    public String readString(int size) {
        checkSize(size);

        final String result;
        try {
            result = new String(buf, pos, size, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw JavaUtils.sneakyThrow(e);
        }
        pos += size;

        return result;
    }

    @Override
    public byte readRawByte() {
        return read();
    }

    @Override
    public void readRawBytes(byte[] value, int offset, int len) {
        read(value, offset, len);
    }

    @Override
    public void skipRawBytes(int size) {
        skip(size);
    }

    /* ########## 自己的方法 ########## */

    public byte read() {
        if (pos >= limit) {
            throw new IndexOutOfBoundsException("Not enough readable bytes.");
        }

        return buf[pos++];
    }

    public void read(byte b[], int off, int len) {
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

    public void skip(int len) {
        checkSize(len);

        pos += len;
    }

    public int available() {
        return limit - pos;
    }

    public void checkSize(int size) {
        if (size < 0) {
            throw new SerializationException("Negative size: " + size);
        }
        if (size > limit - pos) {
            throw new IndexOutOfBoundsException("Not enough readable bytes.");
        }
    }

}
