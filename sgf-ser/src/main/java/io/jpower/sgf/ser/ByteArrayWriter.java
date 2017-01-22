package io.jpower.sgf.ser;

import java.util.Arrays;

/**
 * 包装了一个字节数组
 *
 * @author zheng.sun
 */
class ByteArrayWriter extends CodedWriter {

    /**
     * The maximum size of array to allocate. Some VMs reserve some header words
     * in an array. Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private byte buf[];

    private int count;

    public ByteArrayWriter() {
        this(128);
    }

    public ByteArrayWriter(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Negative initialCapacity: " + initialCapacity);
        }
        this.buf = new byte[initialCapacity];
    }

    /* ########## 实现父类方法 ########## */

    public void writeRawByte(final byte value) {
        write(value);
    }

    public void writeRawBytes(final byte[] value, int offset, int length) {
        write(value, offset, length);
    }

    /* ########## 自己的方法 ########## */

    private void ensureCapacity(int minCapacity) {
        // overflow-conscious code
        if (minCapacity - buf.length > 0) {
            // overflow-conscious code
            int oldCapacity = buf.length;
            int newCapacity = oldCapacity << 1;
            if (newCapacity < 0) { // overflow
                throw new OutOfMemoryError();
            }
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                newCapacity = hugeCapacity(minCapacity);
            }
            buf = Arrays.copyOf(buf, newCapacity);
        }
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) { // overflow
            throw new OutOfMemoryError();
        }
        return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }

    public void write(byte b) {
        int newCount = count + 1;
        ensureCapacity(newCount);
        buf[count] = b;
        count = newCount;
    }

    public void write(byte b[], int offset, int length) {
        if ((offset < 0) || (offset > b.length) || (length < 0)
                || ((offset + length) - b.length > 0)) {
            throw new IndexOutOfBoundsException();
        }
        int newCount = count + length;
        ensureCapacity(newCount);
        System.arraycopy(b, offset, buf, count, length);
        count = newCount;
    }

    public void reset() {
        count = 0;
    }

    public int size() {
        return count;
    }

    public byte[] buf() {
        return buf;
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(buf, count);
    }

}
