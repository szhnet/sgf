package io.jpower.sgf.ser;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.jpower.sgf.utils.JavaUtils;

/**
 * 包装了一个<code>InputStream</code>
 *
 * @author zheng.sun
 */
public class StreamReader extends CodedReader {

    public static final int DEFAULT_BUFFER_SIZE = 4096;

    private final InputStream input;

    private final byte[] buffer;

    private int position;

    private int limit;

    public StreamReader(InputStream input) {
        this(input, DEFAULT_BUFFER_SIZE);
    }

    public StreamReader(InputStream input, int bufferSize) {
        if (bufferSize < 8) {
            throw new IllegalArgumentException("bufferSize < 8: " + bufferSize); // 代码中最多需要一次性加载8个字节，所以必须不能小于8
        }
        this.input = input;
        this.buffer = new byte[bufferSize];
        this.position = 0;
        this.limit = 0;
    }

    /* ########## 实现父类方法 ########## */

    @Override
    public String readString(int size) {
        if (size <= (limit - position) && size > 0) {
            // Fast path: We already have the bytes in a contiguous buffer, so
            // just copy directly from it.
            String result;
            try {
                result = new String(buffer, position, size, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw JavaUtils.sneakyThrow(e);
            }
            position += size;
            return result;
        } else if (size == 0) {
            return "";
        } else {
            // Slow path: Build a byte array first then copy it.
            try {
                return new String(readRawBytesSlowPath(size), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    @Override
    public byte[] readBytes(int size) {
        if (size <= (limit - position) && size > 0) {
            // Fast path: We already have the bytes in a contiguous buffer, so
            // just copy directly from it.
            final byte[] result = Arrays.copyOfRange(buffer, position, position + size);
            position += size;
            return result;
        } else {
            // Slow path: Build a byte array first then copy it.
            return readRawBytesSlowPath(size);
        }
    }

    @Override
    public int readRawVarint32() {
        // See implementation notes for readRawVarint64
        fastpath:
        {
            int pos = position;

            if (limit == pos) {
                break fastpath;
            }

            final byte[] buffer = this.buffer;
            int x;
            if ((x = buffer[pos++]) >= 0) {
                position = pos;
                return x;
            } else if (limit - pos < 9) {
                break fastpath;
            } else if ((x ^= (buffer[pos++] << 7)) < 0L) {
                x ^= (~0L << 7);
            } else if ((x ^= (buffer[pos++] << 14)) >= 0L) {
                x ^= (~0L << 7) ^ (~0L << 14);
            } else if ((x ^= (buffer[pos++] << 21)) < 0L) {
                x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21);
            } else {
                int y = buffer[pos++];
                x ^= y << 28;
                x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21) ^ (~0L << 28);
                if (y < 0 && buffer[pos++] < 0 && buffer[pos++] < 0 && buffer[pos++] < 0
                        && buffer[pos++] < 0 && buffer[pos++] < 0) {
                    break fastpath; // Will throw malformedVarint()
                }
            }
            position = pos;
            return x;
        }
        return (int) readRawVarint64SlowPath();
    }

    @Override
    public long readRawVarint64() {
        // Implementation notes:
        //
        // Optimized for one-byte values, expected to be common.
        // The particular code below was selected from various candidates
        // empirically, by winning VarintBenchmark.
        //
        // Sign extension of (signed) Java bytes is usually a nuisance, but
        // we exploit it here to more easily obtain the sign of bytes read.
        // Instead of cleaning up the sign extension bits by masking eagerly,
        // we delay until we find the final (positive) byte, when we clear all
        // accumulated bits with one xor. We depend on javac to constant fold.
        fastpath:
        {
            int pos = position;

            if (limit == pos) {
                break fastpath;
            }

            final byte[] buffer = this.buffer;
            long x;
            int y;
            if ((y = buffer[pos++]) >= 0) {
                position = pos;
                return y;
            } else if (limit - pos < 9) {
                break fastpath;
            } else if ((x = y ^ (buffer[pos++] << 7)) < 0L) {
                x ^= (~0L << 7);
            } else if ((x ^= (buffer[pos++] << 14)) >= 0L) {
                x ^= (~0L << 7) ^ (~0L << 14);
            } else if ((x ^= (buffer[pos++] << 21)) < 0L) {
                x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21);
            } else if ((x ^= ((long) buffer[pos++] << 28)) >= 0L) {
                x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21) ^ (~0L << 28);
            } else if ((x ^= ((long) buffer[pos++] << 35)) < 0L) {
                x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21) ^ (~0L << 28) ^ (~0L << 35);
            } else if ((x ^= ((long) buffer[pos++] << 42)) >= 0L) {
                x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21) ^ (~0L << 28) ^ (~0L << 35)
                        ^ (~0L << 42);
            } else if ((x ^= ((long) buffer[pos++] << 49)) < 0L) {
                x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21) ^ (~0L << 28) ^ (~0L << 35)
                        ^ (~0L << 42) ^ (~0L << 49);
            } else {
                x ^= ((long) buffer[pos++] << 56);
                x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21) ^ (~0L << 28) ^ (~0L << 35)
                        ^ (~0L << 42) ^ (~0L << 49) ^ (~0L << 56);
                if (x < 0L) {
                    if (buffer[pos++] < 0L) {
                        break fastpath; // Will throw malformedVarint()
                    }
                }
            }
            position = pos;
            return x;
        }
        return readRawVarint64SlowPath();
    }

    @Override
    public short readRawLittleEndian16() {
        int pos = position;

        // hand-inlined ensureAvailable(4);
        if (limit - pos < 2) {
            refillBuffer(2);
            pos = position;
        }

        final byte[] buffer = this.buffer;
        position = pos + 2;
        return (short) (((buffer[pos] & 0xff)) | ((buffer[pos + 1] & 0xff) << 8));
    }

    @Override
    public int readRawLittleEndian32() {
        int pos = position;

        // hand-inlined ensureAvailable(4);
        if (limit - pos < 4) {
            refillBuffer(4);
            pos = position;
        }

        final byte[] buffer = this.buffer;
        position = pos + 4;
        return (((buffer[pos] & 0xff)) | ((buffer[pos + 1] & 0xff) << 8)
                | ((buffer[pos + 2] & 0xff) << 16) | ((buffer[pos + 3] & 0xff) << 24));
    }

    @Override
    public long readRawLittleEndian64() {
        int pos = position;

        // hand-inlined ensureAvailable(8);
        if (limit - pos < 8) {
            refillBuffer(8);
            pos = position;
        }

        final byte[] buffer = this.buffer;
        position = pos + 8;
        return ((((long) buffer[pos] & 0xffL)) | (((long) buffer[pos + 1] & 0xffL) << 8)
                | (((long) buffer[pos + 2] & 0xffL) << 16)
                | (((long) buffer[pos + 3] & 0xffL) << 24)
                | (((long) buffer[pos + 4] & 0xffL) << 32)
                | (((long) buffer[pos + 5] & 0xffL) << 40)
                | (((long) buffer[pos + 6] & 0xffL) << 48)
                | (((long) buffer[pos + 7] & 0xffL) << 56));
    }

    @Override
    public byte readRawByte() {
        if (position == limit) {
            refillBuffer(1);
        }
        return buffer[position++];
    }

    @Override
    public void readRawBytes(byte[] value, int offset, int len) {
        if (len <= (limit - position) && len > 0) {
            // Fast path: We already have the bytes in a contiguous buffer, so
            // just copy directly from it.
            System.arraycopy(buffer, position, value, offset, len);
            position += len;
        } else {
            // Slow path: Build a byte array first then copy it.
            readRawBytesSlowPath(value, offset, len);
        }
    }

    @Override
    public void skipRawVarint() {
        if (limit - position >= 10) {
            final byte[] buffer = this.buffer;
            int pos = position;
            for (int i = 0; i < 10; i++) {
                if (buffer[pos++] >= 0) {
                    position = pos;
                    return;
                }
            }
        }
        skipRawVarintSlowPath();
    }

    @Override
    public void skipRawBytes(int size) {
        if (size <= (limit - position) && size >= 0) {
            // We have all the bytes we need already.
            position += size;
        } else {
            skipRawBytesSlowPath(size);
        }
    }

    /* ########## 自己的方法 ########## */

    /**
     * Ensures that at least {@code n} bytes are available in the buffer,
     * reading more bytes from the input if necessary to make it so. Caller must
     * ensure that the requested space is less than BUFFER_SIZE.
     *
     * @throws InvalidProtocolBufferException The end of the stream or the current limit was reached.
     */
    private void ensureAvailable(int n) {
        if (limit - position < n) {
            refillBuffer(n);
        }
    }

    /**
     * Reads more bytes from the input, making at least {@code n} bytes
     * available in the buffer. Caller must ensure that the requested space is
     * not yet available, and that the requested space is less than BUFFER_SIZE.
     *
     * @throws InvalidProtocolBufferException The end of the stream or the current limit was reached.
     */
    private void refillBuffer(int n) {
        if (!tryRefillBuffer(n)) {
            throw SerializationException.truncatedMessage();
        }
    }

    /**
     * Tries to read more bytes from the input, making at least {@code n} bytes
     * available in the buffer. Caller must ensure that the requested space is
     * not yet available, and that the requested space is less than BUFFER_SIZE.
     *
     * @return {@code true} if the bytes could be made available; {@code false}
     * if the end of the stream or the current limit was reached.
     */
    private boolean tryRefillBuffer(int n) {
        if (position + n <= limit) {
            throw new IllegalStateException(
                    "refillBuffer() called when " + n + " bytes were already available in buffer");
        }

        int pos = position;
        if (pos > 0) {
            if (limit > pos) { // 去掉position之前的，也就是读取过的数据
                System.arraycopy(buffer, pos, buffer, 0, limit - pos);
            }
            limit -= pos;
            position = 0;
        }

        int bytesRead;
        try {
            bytesRead = input.read(buffer, limit, buffer.length - limit);
        } catch (IOException e) {
            throw JavaUtils.sneakyThrow(e);
        }
        if (bytesRead == 0 || bytesRead < -1 || bytesRead > buffer.length) {
            throw new IllegalStateException("InputStream#read(byte[]) returned invalid result: "
                    + bytesRead + "\nThe InputStream implementation is buggy.");
        }
        if (bytesRead > 0) {
            limit += bytesRead;
            return (limit >= n) ? true : tryRefillBuffer(n);
        }
        return false;
    }

    /**
     * Exactly like readRawBytes, but caller must have already checked the fast
     * path: (size <= (bufferSize - pos) && size > 0)
     */
    private byte[] readRawBytesSlowPath(int size) {
        if (size <= 0) {
            if (size == 0) {
                return Utils.EMPTY_BYTES;
            } else {
                throw new SerializationException("Negative size: " + size);
            }
        }

        if (size < buffer.length) {
            // Reading more bytes than are in the buffer, but not an excessive
            // number
            // of bytes. We can safely allocate the resulting array ahead of
            // time.

            // First copy what we have.
            final byte[] bytes = new byte[size];
            int pos = limit - position;
            System.arraycopy(buffer, position, bytes, 0, pos);
            position = limit;

            // We want to refill the buffer and then copy from the buffer into
            // our
            // byte array rather than reading directly into our byte array
            // because
            // the input may be unbuffered.
            ensureAvailable(size - pos);
            System.arraycopy(buffer, 0, bytes, pos, size - pos);
            position = size - pos;

            return bytes;
        } else {
            // The size is very large. For security reasons, we can't allocate
            // the
            // entire byte array yet. The size comes directly from the input, so
            // a
            // maliciously-crafted message could provide a bogus very large size
            // in
            // order to trick the app into allocating a lot of memory. We avoid
            // this
            // by allocating and reading only a small chunk at a time, so that
            // the
            // malicious message must actually *be* extremely large to cause
            // problems. Meanwhile, we limit the allowed size of a message
            // elsewhere.

            // Remember the buffer markers since we'll have to copy the bytes
            // out of
            // it later.
            final int originalBufferPos = position;
            final int originalBufferSize = limit;

            position = 0;
            limit = 0;

            // Read all the rest of the bytes we need.
            int sizeLeft = size - (originalBufferSize - originalBufferPos);
            final List<byte[]> chunks = new ArrayList<byte[]>();

            while (sizeLeft > 0) {
                final byte[] chunk = new byte[Math.min(sizeLeft, DEFAULT_BUFFER_SIZE)];
                int pos = 0;
                while (pos < chunk.length) {
                    int n;
                    try {
                        n = (input == null) ? -1 : input.read(chunk, pos, chunk.length - pos);
                    } catch (IOException e) {
                        throw JavaUtils.sneakyThrow(e);
                    }
                    if (n == -1) {
                        throw SerializationException.truncatedMessage();
                    }
                    pos += n;
                }
                sizeLeft -= chunk.length;
                chunks.add(chunk);
            }

            // OK, got everything. Now concatenate it all into one buffer.
            final byte[] bytes = new byte[size];

            // Start by copying the leftover bytes from this.buffer.
            int pos = originalBufferSize - originalBufferPos;
            System.arraycopy(buffer, originalBufferPos, bytes, 0, pos);

            // And now all the chunks.
            for (final byte[] chunk : chunks) {
                System.arraycopy(chunk, 0, bytes, pos, chunk.length);
                pos += chunk.length;
            }

            // Done.
            return bytes;
        }
    }

    private void readRawBytesSlowPath(byte[] value, int offset, int len) {
        if (len <= 0) {
            if (len == 0) {
                return;
            } else {
                throw SerializationException.negativeSize(len);
            }
        }

        if (len < buffer.length) {
            // Reading more bytes than are in the buffer, but not an excessive
            // number
            // of bytes. We can safely allocate the resulting array ahead of
            // time.

            // First copy what we have.
            int pos = limit - position;
            System.arraycopy(buffer, position, value, offset, pos);
            position = limit;

            // We want to refill the buffer and then copy from the buffer into
            // our
            // byte array rather than reading directly into our byte array
            // because
            // the input may be unbuffered.
            ensureAvailable(len - pos);
            System.arraycopy(buffer, 0, value, pos, len - pos);
            position = len - pos;
        } else {
            // The size is very large. For security reasons, we can't allocate
            // the
            // entire byte array yet. The size comes directly from the input, so
            // a
            // maliciously-crafted message could provide a bogus very large size
            // in
            // order to trick the app into allocating a lot of memory. We avoid
            // this
            // by allocating and reading only a small chunk at a time, so that
            // the
            // malicious message must actually *be* extremely large to cause
            // problems. Meanwhile, we limit the allowed size of a message
            // elsewhere.

            // Remember the buffer markers since we'll have to copy the bytes
            // out of
            // it later.
            final int originalBufferPos = position;
            final int originalBufferSize = limit;

            position = 0;
            limit = 0;

            // Start by copying the leftover bytes from this.buffer.
            int pos = originalBufferSize - originalBufferPos;
            System.arraycopy(buffer, originalBufferPos, value, offset, pos);

            try {
                input.read(value, offset + pos, len - pos);
            } catch (IOException e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    /**
     * Variant of readRawVarint64 for when uncomfortably close to the limit.
     */
    /* Visible for testing */
    long readRawVarint64SlowPath() {
        long result = 0;
        for (int shift = 0; shift < 64; shift += 7) {
            final byte b = readRawByte();
            result |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
        }
        throw new SerializationException("Reader encountered a malformed varint.");
    }

    /**
     * Reads and discards {@code size} bytes.
     *
     * @throws SerializationException The end of the stream or the current limit was reached.
     */
    private void skipRawVarintSlowPath() {
        for (int i = 0; i < 10; i++) {
            if (readRawByte() >= 0) {
                return;
            }
        }
        throw SerializationException.malformedVarint();
    }

    /**
     * Exactly like skipRawBytes, but caller must have already checked the fast
     * path: (size <= (bufferSize - pos) && size >= 0)
     */
    private void skipRawBytesSlowPath(final int size) {
        if (size < 0) {
            throw SerializationException.negativeSize(size);
        }

        // Skipping more bytes than are in the buffer. First skip what we have.
        int pos = limit - position;
        position = limit;

        // Keep refilling the buffer until we get to the point we wanted to skip
        // to.
        // This has the side effect of ensuring the limits are updated
        // correctly.
        refillBuffer(1);
        while (size - pos > limit) {
            pos += limit; // 全部跳过
            position = limit;
            refillBuffer(1);
        }

        position = size - pos;
    }

}
