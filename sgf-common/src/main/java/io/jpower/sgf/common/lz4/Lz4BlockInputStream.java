package io.jpower.sgf.common.lz4;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.jpountz.lz4.LZ4Exception;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import net.jpountz.util.SafeUtils;

/**
 * <a href=" http://www.lz4.org">LZ4</a> InputStream
 * <p>
 * <p>
 * 格式： Compress method + Decompressed length + Compressed length + Compressed
 * data
 * <p>
 * 如果Compress method为<code>Lz4Consts.COMPRESS_METHOD_NONE</code>，那么将没有Compressed
 * length字段， 因为此时Decompressed length与Compressed length相等。
 * <p>
 * 数据以外的部分都将使用Varint进行编码。
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class Lz4BlockInputStream extends FilterInputStream {

    private final LZ4FastDecompressor decompressor;

    private byte[] buffer;

    private byte[] compressedBuffer;

    private int decompressedLength;

    private int offset;

    private boolean finished = false;

    private boolean reachEOF = false;

    public Lz4BlockInputStream(InputStream in, LZ4FastDecompressor decompressor) {
        super(in);
        this.decompressor = decompressor;
        this.buffer = new byte[64];
        this.compressedBuffer = new byte[64];
        this.offset = this.decompressedLength = 0;
    }

    public Lz4BlockInputStream(InputStream in) {
        this(in, LZ4Factory.fastestInstance().fastDecompressor());
    }

    private void ensureNotFinished() {
        if (finished) {
            throw new IllegalStateException("This stream is already closed");
        }
    }

    @Override
    public int available() throws IOException {
        ensureNotFinished();
        if (reachEOF) {
            return 0;
        }
        return decompressedLength - offset;
    }

    public int read() throws IOException {
        ensureNotFinished();
        if (reachEOF) {
            return -1;
        }
        if (offset == decompressedLength) {
            refill();
        }
        if (reachEOF) {
            return -1;
        }
        return buffer[offset++] & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        ensureNotFinished();
        SafeUtils.checkRange(b, off, len);

        if (reachEOF) {
            return -1;
        }
        if (offset == decompressedLength) {
            refill();
        }
        if (reachEOF) {
            return -1;
        }
        len = Math.min(len, decompressedLength - offset);
        System.arraycopy(buffer, offset, b, off, len);
        offset += len;
        return len;
    }

    @Override
    public long skip(long n) throws IOException {
        ensureNotFinished();
        if (reachEOF) {
            return 0;
        }
        if (offset == decompressedLength) {
            refill();
        }
        if (reachEOF) {
            return 0;
        }
        final int skipped = (int) Math.min(n, decompressedLength - offset);
        offset += skipped;
        return skipped;
    }

    private void refill() throws IOException {
        final int compressMethod = readVarint(); // compressMethod
        if (reachEOF) {
            return;
        }
        if (compressMethod != Lz4Consts.COMPRESS_METHOD_NONE
                && compressMethod != Lz4Consts.COMPRESS_METHOD_LZ4) {
            throw new IOException("Stream is corrupted");
        }

        decompressedLength = readVarint(); // decompressed length
        if (decompressedLength > Lz4Consts.MAX_BLOCK_SIZE) {
            throw new IllegalArgumentException("blockSize is too large. " + decompressedLength
                    + " > " + Lz4Consts.MAX_BLOCK_SIZE);
        }
        final int compressedLen;
        if (compressMethod == Lz4Consts.COMPRESS_METHOD_LZ4) {
            compressedLen = readVarint(); // compressed length
        } else {
            compressedLen = decompressedLength;
        }
        if (compressedLen > Lz4Consts.MAX_BLOCK_SIZE) {
            throw new IllegalArgumentException("compressedLength is too large. " + compressedLen
                    + " > " + Lz4Consts.MAX_BLOCK_SIZE);
        }
        if (decompressedLength < 0 || compressedLen < 0
                || (decompressedLength == 0 && compressedLen != 0)
                || (decompressedLength != 0 && compressedLen == 0)
                || (compressMethod == Lz4Consts.COMPRESS_METHOD_NONE
                && decompressedLength != compressedLen)) {
            throw new IOException("Stream is corrupted");
        }
        if (buffer.length < decompressedLength) {
            buffer = new byte[Math.max(decompressedLength, buffer.length * 3 / 2)];
        }

        // compressed data
        switch (compressMethod) {
            case Lz4Consts.COMPRESS_METHOD_NONE:
                readFully(buffer, decompressedLength);
                break;
            case Lz4Consts.COMPRESS_METHOD_LZ4:
                if (compressedBuffer.length < compressedLen) {
                    compressedBuffer = new byte[Math.max(compressedLen,
                            compressedBuffer.length * 3 / 2)];
                }
                readFully(compressedBuffer, compressedLen);
                try {
                    final int compressedLen2 = decompressor.decompress(compressedBuffer, 0, buffer, 0,
                            decompressedLength);
                    if (compressedLen != compressedLen2) {
                        throw new IOException("Stream is corrupted");
                    }
                } catch (LZ4Exception e) {
                    throw new IOException("Stream is corrupted", e);
                }
                break;
            default:
                throw new IOException("compress method mismatch");
        }
        offset = 0;
    }

    private void readFully(byte[] b, int len) throws IOException {
        int read = 0;
        while (read < len) {
            final int r = in.read(b, read, len - read);
            if (r < 0) {
                throw new EOFException("Stream ended prematurely");
            }
            read += r;
        }
        assert len == read;
    }

    private int readVarint() throws IOException {
        int result = 0;
        int shift = 0;
        for (; shift < 29; shift += 7) {
            int read = in.read();
            if (read == -1) {
                reachEOF = true;
                return -1;
            }
            final byte b = (byte) read;
            result |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
        }
        throw new IOException("encountered a malformed varint");
    }

    public void close() throws IOException {
        if (!finished) {
            finish();
        }
        if (in != null) {
            try {
                in.close();
            } finally {
                in = null;
            }
        }
    }

    private void finish() {
        ensureNotFinished();
        finished = true;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void mark(int readlimit) {
        // unsupported
    }

    @Override
    public void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(in=" + in + ", decompressor=" + decompressor + ")";
    }

}
