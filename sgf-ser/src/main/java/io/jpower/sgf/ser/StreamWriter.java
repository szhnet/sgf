package io.jpower.sgf.ser;

import java.io.IOException;
import java.io.OutputStream;

import io.jpower.sgf.utils.JavaUtils;

/**
 * 包装了一个<code>InputStream</code>
 *
 * @author zheng.sun
 */
public class StreamWriter extends CodedWriter {

    public static final int DEFAULT_BUFFER_SIZE = 4096;

    private final OutputStream output;

    private final byte[] buffer;

    private final int limit;

    private int position;

    private int totalBytesWritten = 0;

    public StreamWriter(OutputStream output) {
        this(output, DEFAULT_BUFFER_SIZE);
    }

    public StreamWriter(OutputStream output, int bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("bufferSize <= 0: " + bufferSize);
        }
        this.output = output;
        this.buffer = new byte[bufferSize];
        this.position = 0;
        this.limit = bufferSize;
    }

    /* ########## 实现父类方法 ########## */

    @Override
    public void writeRawByte(byte value) {
        if (position == limit) {
            flush();
        }

        buffer[position++] = value;
        ++totalBytesWritten;
    }

    @Override
    public void writeRawBytes(byte[] value, int offset, int length) {
        if (limit - position >= length) {
            // We have room in the current buffer.
            System.arraycopy(value, offset, buffer, position, length);
            position += length;
            totalBytesWritten += length;
        } else {
            // Write extends past current buffer. Fill the rest of this buffer
            // and
            // flush.
            final int bytesWritten = limit - position;
            System.arraycopy(value, offset, buffer, position, bytesWritten);
            offset += bytesWritten;
            length -= bytesWritten;
            position = limit;
            totalBytesWritten += bytesWritten;
            flush();

            // Now deal with the rest.
            if (length <= limit) {
                // Fits in new buffer.
                System.arraycopy(value, offset, buffer, 0, length);
                position = length;
            } else {
                // Write is very big. Let's do it all at once.
                try {
                    output.write(value, offset, length);
                } catch (IOException e) {
                    throw JavaUtils.sneakyThrow(e);
                }
            }
            totalBytesWritten += length;
        }
    }

    /* ########## 自己的方法 ########## */

    /**
     * Flushes the stream and forces any buffered bytes to be written. This does
     * not flush the underlying OutputStream.
     */
    public void flush() {
        try {
            output.write(buffer, 0, position);
        } catch (IOException e) {
            throw JavaUtils.sneakyThrow(e);
        }
        position = 0;
    }

    /**
     * Get the total number of bytes successfully written to this stream. The
     * returned value is not guaranteed to be accurate if exceptions have been
     * found in the middle of writing.
     */
    public int getTotalBytesWritten() {
        return totalBytesWritten;
    }

}
