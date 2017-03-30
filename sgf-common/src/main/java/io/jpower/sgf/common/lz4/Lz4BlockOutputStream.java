package io.jpower.sgf.common.lz4;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.util.SafeUtils;

/**
 * <a href=" http://www.lz4.org">LZ4</a> OutputStream
 * <p>
 * <p>
 * 格式： Compress method + Decompressed length + Compressed length + Compressed data
 * <p>
 * 如果Compress method为<code>Lz4Constants.COMPRESS_METHOD_NONE</code>，那么将没有Compressed
 * length字段， 因为此时Decompressed length与Compressed length相等。
 * <p>
 * 数据以外的部分都将使用Varint进行编码。
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class Lz4BlockOutputStream extends FilterOutputStream {

    private final int blockSize;

    private final LZ4Compressor compressor;

    private final byte[] buffer;

    private final byte[] compressedBuffer;

    private final boolean syncFlush;

    private boolean finished = false;

    private int currentBlockLength;

    public Lz4BlockOutputStream(OutputStream out, int blockSize, LZ4Compressor compressor,
                                boolean syncFlush) {
        super(out);

        if (blockSize < Lz4Constants.MIN_BLOCK_SIZE) {
            throw new IllegalArgumentException(
                    "blockSize must be >= " + Lz4Constants.MIN_BLOCK_SIZE + ", got " + blockSize);
        }
        if (blockSize > Lz4Constants.MAX_BLOCK_SIZE) {
            throw new IllegalArgumentException(
                    "blockSize must be <= " + Lz4Constants.MAX_BLOCK_SIZE + ", got " + blockSize);
        }
        this.blockSize = blockSize;
        this.compressor = compressor;
        this.buffer = new byte[blockSize];
        final int compressedBlockSize = compressor.maxCompressedLength(blockSize);
        this.compressedBuffer = new byte[compressedBlockSize];
        this.syncFlush = syncFlush;
        this.currentBlockLength = 0;
    }

    public Lz4BlockOutputStream(OutputStream out, int blockSize, LZ4Compressor compressor) {
        this(out, blockSize, compressor, false);
    }

    public Lz4BlockOutputStream(OutputStream out, int blockSize) {
        this(out, blockSize, LZ4Factory.fastestInstance().fastCompressor());
    }

    public Lz4BlockOutputStream(OutputStream out) {
        this(out, 1 << 64); // 64KB
    }

    private void ensureNotFinished() {
        if (finished) {
            throw new IllegalStateException("This stream is already closed");
        }
    }

    @Override
    public void write(int b) throws IOException {
        ensureNotFinished();
        if (currentBlockLength == blockSize) {
            flushBufferedData();
        }
        buffer[currentBlockLength++] = (byte) b;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        SafeUtils.checkRange(b, off, len);
        ensureNotFinished();

        if (currentBlockLength == blockSize) {
            flushBufferedData();
        }

        while (currentBlockLength + len > blockSize) {
            final int l = blockSize - currentBlockLength;
            System.arraycopy(b, off, buffer, currentBlockLength, l);
            currentBlockLength = blockSize;
            flushBufferedData();
            off += l;
            len -= l;
        }
        System.arraycopy(b, off, buffer, currentBlockLength, len);
        currentBlockLength += len;
    }

    @Override
    public void write(byte[] b) throws IOException {
        ensureNotFinished();
        write(b, 0, b.length);
    }

    @Override
    public void close() throws IOException {
        if (!finished) {
            finish();
        }
        if (out != null) {
            try {
                out.close();
            } finally {
                out = null;
            }
        }
    }

    @Override
    public void flush() throws IOException {
        if (out != null) {
            if (syncFlush) {
                flushBufferedData();
            }
            out.flush();
        }
    }

    private void flushBufferedData() throws IOException {
        if (currentBlockLength == 0) {
            return;
        }
        int compressedLength = compressor.compress(buffer, 0, currentBlockLength, compressedBuffer,
                0);
        final int compressMethod;
        if (compressedLength >= currentBlockLength) {
            compressMethod = Lz4Constants.COMPRESS_METHOD_NONE;
            compressedLength = currentBlockLength;
        } else {
            compressMethod = Lz4Constants.COMPRESS_METHOD_LZ4;
        }

        writeVarint(compressMethod); // compressMethod
        writeVarint(currentBlockLength); // decompressed length
        if (compressMethod == Lz4Constants.COMPRESS_METHOD_LZ4) {
            writeVarint(compressedLength); // compressed length
            out.write(compressedBuffer, 0, compressedLength); // compressed data
        } else {
            out.write(buffer, 0, currentBlockLength);
        }

        currentBlockLength = 0;
    }

    private void finish() throws IOException {
        ensureNotFinished();
        flushBufferedData();
        finished = true;
        out.flush();
    }

    private void writeVarint(int value) throws IOException {
        while (true) {
            if ((value & ~0x7F) == 0) {
                out.write(value);
                return;
            } else {
                out.write((value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(out=" + out + ", blockSize=" + blockSize
                + ", compressor=" + compressor + ")";
    }

}
