package io.jpower.sgf.ser;

import static io.jpower.sgf.ser.WireFormat.FIXED_16_SIZE;
import static io.jpower.sgf.ser.WireFormat.FIXED_32_SIZE;
import static io.jpower.sgf.ser.WireFormat.FIXED_64_SIZE;
import static io.jpower.sgf.ser.WireFormat.MAX_VARINT_SIZE;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.jpower.sgf.utils.JavaUtils;

/**
 * 包装了一个<code>InputStream</code>
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
class StreamReader extends CodedReader {

    static final int DEFAULT_BUFFER_SIZE = 4096;

    private final InputStream input;

    private final byte[] buffer;

    private int position;

    private int limit;

    StreamReader(InputStream input) {
        this(input, DEFAULT_BUFFER_SIZE);
    }

    StreamReader(InputStream input, int bufferSize) {
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
    String readString(int size) {
        if (size > 0 && size <= (limit - position)) {
            // Fast path: We already have the bytes in a contiguous buffer, so
            // just copy directly from it.
            String result = new String(buffer, position, size, Utils.CHARSET);
            position += size;
            return result;
        }
        if (size == 0) {
            return "";
        }
        if (size <= limit) {
            refillBuffer(size);
            String result = new String(buffer, position, size, Utils.CHARSET);
            position += size;
            return result;
        }
        // Slow path: Build a byte array first then copy it.
        return new String(readRawBytesSlowPath(size), Utils.CHARSET);

    }

    @Override
    byte[] readBytes(int size) {
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
    int readRawVarint32() {
        // See implementation notes for readRawVarint64
        fastpath:
        {
            int tempPos = position;

            if (limit == tempPos) {
                break fastpath;
            }

            final byte[] buffer = this.buffer;
            int x;
            if ((x = buffer[tempPos++]) >= 0) {
                position = tempPos;
                return x;
            } else if (limit - tempPos < 9) {
                break fastpath;
            } else if ((x ^= (buffer[tempPos++] << 7)) < 0) {
                x ^= (~0 << 7);
            } else if ((x ^= (buffer[tempPos++] << 14)) >= 0) {
                x ^= (~0 << 7) ^ (~0L << 14);
            } else if ((x ^= (buffer[tempPos++] << 21)) < 0) {
                x ^= (~0 << 7) ^ (~0 << 14) ^ (~0 << 21);
            } else {
                int y = buffer[tempPos++];
                x ^= y << 28;
                x ^= (~0 << 7) ^ (~0 << 14) ^ (~0L << 21) ^ (~0 << 28);
                if (y < 0
                        && buffer[tempPos++] < 0
                        && buffer[tempPos++] < 0
                        && buffer[tempPos++] < 0
                        && buffer[tempPos++] < 0
                        && buffer[tempPos++] < 0) {
                    break fastpath; // Will throw malformedVarint()
                }
            }
            position = tempPos;
            return x;
        }
        return (int) readRawVarint64SlowPath();
    }

    @Override
    long readRawVarint64() {
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
            int tempPos = position;

            if (limit == tempPos) {
                break fastpath;
            }

            final byte[] buffer = this.buffer;
            long x;
            int y;
            if ((y = buffer[tempPos++]) >= 0) {
                position = tempPos;
                return y;
            } else if (limit - tempPos < 9) {
                break fastpath;
            } else if ((x = y ^ (buffer[tempPos++] << 7)) < 0) {
                x ^= (~0 << 7);
            } else if ((x ^= (buffer[tempPos++] << 14)) >= 0) {
                x ^= (~0 << 7) ^ (~0 << 14);
            } else if ((x ^= (buffer[tempPos++] << 21)) < 0) {
                x ^= (~0 << 7) ^ (~0 << 14) ^ (~0 << 21);
            } else if ((x ^= ((long) buffer[tempPos++] << 28)) >= 0L) {
                x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21) ^ (~0L << 28);
            } else if ((x ^= ((long) buffer[tempPos++] << 35)) < 0L) {
                x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21) ^ (~0L << 28) ^ (~0L << 35);
            } else if ((x ^= ((long) buffer[tempPos++] << 42)) >= 0L) {
                x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21) ^ (~0L << 28) ^ (~0L << 35) ^ (~0L << 42);
            } else if ((x ^= ((long) buffer[tempPos++] << 49)) < 0L) {
                x ^=
                        (~0L << 7)
                                ^ (~0L << 14)
                                ^ (~0L << 21)
                                ^ (~0L << 28)
                                ^ (~0L << 35)
                                ^ (~0L << 42)
                                ^ (~0L << 49);
            } else {
                x ^= ((long) buffer[tempPos++] << 56);
                x ^=
                        (~0L << 7)
                                ^ (~0L << 14)
                                ^ (~0L << 21)
                                ^ (~0L << 28)
                                ^ (~0L << 35)
                                ^ (~0L << 42)
                                ^ (~0L << 49)
                                ^ (~0L << 56);
                if (x < 0L) {
                    if (buffer[tempPos++] < 0L) {
                        break fastpath; // Will throw malformedVarint()
                    }
                }
            }
            position = tempPos;
            return x;
        }
        return readRawVarint64SlowPath();
    }

    @Override
    short readRawLittleEndian16() {
        int tempPos = position;

        if (limit - tempPos < FIXED_16_SIZE) {
            refillBuffer(FIXED_16_SIZE);
            tempPos = position;
        }

        final byte[] buffer = this.buffer;
        position = tempPos + FIXED_16_SIZE;
        return (short) (((buffer[tempPos] & 0xff))
                | ((buffer[tempPos + 1] & 0xff) << 8));
    }

    @Override
    int readRawLittleEndian32() {
        int tempPos = position;

        if (limit - tempPos < FIXED_32_SIZE) {
            refillBuffer(FIXED_32_SIZE);
            tempPos = position;
        }

        final byte[] buffer = this.buffer;
        position = tempPos + FIXED_32_SIZE;
        return (((buffer[tempPos] & 0xff))
                | ((buffer[tempPos + 1] & 0xff) << 8)
                | ((buffer[tempPos + 2] & 0xff) << 16)
                | ((buffer[tempPos + 3] & 0xff) << 24));
    }

    @Override
    long readRawLittleEndian64() {
        int tempPos = position;

        if (limit - tempPos < FIXED_64_SIZE) {
            refillBuffer(FIXED_64_SIZE);
            tempPos = position;
        }

        final byte[] buffer = this.buffer;
        position = tempPos + FIXED_64_SIZE;
        return (((buffer[tempPos] & 0xffL))
                | ((buffer[tempPos + 1] & 0xffL) << 8)
                | ((buffer[tempPos + 2] & 0xffL) << 16)
                | ((buffer[tempPos + 3] & 0xffL) << 24)
                | ((buffer[tempPos + 4] & 0xffL) << 32)
                | ((buffer[tempPos + 5] & 0xffL) << 40)
                | ((buffer[tempPos + 6] & 0xffL) << 48)
                | ((buffer[tempPos + 7] & 0xffL) << 56));
    }

    @Override
    byte readRawByte() {
        if (position == limit) {
            refillBuffer(1);
        }
        return buffer[position++];
    }

    @Override
    void readRawBytes(byte[] value, int offset, int len) {
        final int tempPos = position;
        if (len <= (limit - tempPos) && len > 0) {
            position = tempPos + len;
            System.arraycopy(buffer, tempPos, value, offset, len);
        } else {
            readRawBytesSlowPath(value, offset, len);
        }
    }

    @Override
    void skipRawVarint() {
        if (limit - position >= MAX_VARINT_SIZE) {
            skipRawVarintFastPath();
        } else {
            skipRawVarintSlowPath();
        }
    }

    @Override
    void skipRawBytes(int size) {
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
     * @throws SerializationException The end of the stream or the current limit was reached.
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
     * @throws SerializationException The end of the stream or the current limit was reached.
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

        int tempPos = position;
        if (tempPos > 0) {
            if (limit > tempPos) { // 去掉position之前的，也就是读取过的数据
                System.arraycopy(buffer, tempPos, buffer, 0, limit - tempPos);
            }
            limit -= tempPos;
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

    private byte[] readRawBytesSlowPath(int size) {
        if (size == 0) {
            return Utils.EMPTY_BYTES;
        }
        if (size < 0) {
            throw SerializationException.negativeSize(size);
        }

        final int originalBufferPos = position;
        final int bufferedBytes = limit - position;

        // Mark the current buffer consumed.
        position = 0;
        limit = 0;

        // Determine the number of bytes we need to read from the input stream.
        int sizeLeft = size - bufferedBytes;
        int inputAvailable;
        try {
            inputAvailable = input.available();
        } catch (IOException e) {
            throw JavaUtils.sneakyThrow(e);
        }
        if (sizeLeft < DEFAULT_BUFFER_SIZE || sizeLeft <= inputAvailable) {
            // Either the bytes we need are known to be available, or the required buffer is
            // within an allowed threshold - go ahead and allocate the buffer now.
            final byte[] bytes = new byte[size];

            // Copy all of the buffered bytes to the result buffer.
            System.arraycopy(buffer, originalBufferPos, bytes, 0, bufferedBytes);

            // Fill the remaining bytes from the input stream.
            int tempPos = bufferedBytes;
            while (tempPos < bytes.length) {
                int n;
                try {
                    n = input.read(bytes, tempPos, size - tempPos);
                } catch (IOException e) {
                    throw JavaUtils.sneakyThrow(e);
                }
                if (n == -1) {
                    throw SerializationException.truncatedMessage();
                }
                tempPos += n;
            }

            return bytes;
        }

        // The size is very large.  For security reasons, we can't allocate the
        // entire byte array yet.  The size comes directly from the input, so a
        // maliciously-crafted message could provide a bogus very large size in
        // order to trick the app into allocating a lot of memory.  We avoid this
        // by allocating and reading only a small chunk at a time, so that the
        // malicious message must actually *be* extremely large to cause
        // problems.  Meanwhile, we limit the allowed size of a message elsewhere.
        final List<byte[]> chunks = new ArrayList<byte[]>();

        while (sizeLeft > 0) {
            final byte[] chunk = new byte[Math.min(sizeLeft, DEFAULT_BUFFER_SIZE)];
            int tempPos = 0;
            while (tempPos < chunk.length) {
                final int n;
                try {
                    n = input.read(chunk, tempPos, chunk.length - tempPos);
                } catch (IOException e) {
                    throw JavaUtils.sneakyThrow(e);
                }
                if (n == -1) {
                    throw SerializationException.truncatedMessage();
                }
                tempPos += n;
            }
            sizeLeft -= chunk.length;
            chunks.add(chunk);
        }

        // OK, got everything.  Now concatenate it all into one buffer.
        final byte[] bytes = new byte[size];

        // Start by copying the leftover bytes from this.buffer.
        System.arraycopy(buffer, originalBufferPos, bytes, 0, bufferedBytes);

        // And now all the chunks.
        int tempPos = bufferedBytes;
        for (final byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, bytes, tempPos, chunk.length);
            tempPos += chunk.length;
        }

        // Done.
        return bytes;
    }

    private void readRawBytesSlowPath(byte[] value, int offset, int len) {
        final int size = len;
        if (size == 0) {
            return;
        }
        if (size < 0) {
            throw SerializationException.negativeSize(size);
        }

        final int originalBufferPos = position;
        final int bufferedBytes = limit - position;

        // Mark the current buffer consumed.
        position = 0;
        limit = 0;

        // Determine the number of bytes we need to read from the input stream.
        int sizeLeft = size - bufferedBytes;
        int inputAvailable;
        try {
            inputAvailable = input.available();
        } catch (IOException e) {
            throw JavaUtils.sneakyThrow(e);
        }
        if (sizeLeft < DEFAULT_BUFFER_SIZE || sizeLeft <= inputAvailable) {
            // Either the bytes we need are known to be available, or the required buffer is
            // within an allowed threshold - go ahead and allocate the buffer now.

            // Copy all of the buffered bytes to the result buffer.
            System.arraycopy(buffer, originalBufferPos, value, offset, bufferedBytes);

            // Fill the remaining bytes from the input stream.
            int tempPos = offset + bufferedBytes;
            while (tempPos < size) {
                int n;
                try {
                    n = input.read(value, tempPos, size - tempPos);
                } catch (IOException e) {
                    throw JavaUtils.sneakyThrow(e);
                }
                if (n == -1) {
                    throw SerializationException.truncatedMessage();
                }
                tempPos += n;
            }
            return;
        }

        // Start by copying the leftover bytes from this.buffer.
        System.arraycopy(buffer, originalBufferPos, value, offset, bufferedBytes);

        int tempPos = offset + bufferedBytes;
        while (tempPos < size) {
            final int n;
            try {
                n = input.read(value, tempPos, size - tempPos);
            } catch (IOException e) {
                throw JavaUtils.sneakyThrow(e);
            }
            if (n == -1) {
                throw SerializationException.truncatedMessage();
            }
            tempPos += n;
        }

        // Done.
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
        throw SerializationException.malformedVarint();
    }

    private void skipRawVarintFastPath() {
        for (int i = 0; i < MAX_VARINT_SIZE; i++) {
            if (buffer[position++] >= 0) {
                return;
            }
        }
        throw SerializationException.malformedVarint();
    }

    /**
     * Reads and discards {@code size} bytes.
     *
     * @throws SerializationException The end of the stream or the current limit was reached.
     */
    private void skipRawVarintSlowPath() {
        for (int i = 0; i < MAX_VARINT_SIZE; i++) {
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
        int tempPos = limit - position;
        position = limit;

        // Keep refilling the buffer until we get to the point we wanted to skip
        // to.
        // This has the side effect of ensuring the limits are updated
        // correctly.
        refillBuffer(1);
        while (size - tempPos > limit) {
            tempPos += limit; // 全部跳过
            position = limit;
            refillBuffer(1);
        }

        position = size - tempPos;
    }

}
