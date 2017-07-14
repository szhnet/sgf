package io.jpower.sgf.net.compress.lz4;

import java.nio.ByteBuffer;

import io.jpower.sgf.net.common.NettyUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import io.jpower.sgf.net.compress.MessageBodyCompressor;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

/**
 * <a href=" http://www.lz4.org">LZ4</a>压缩
 * <p>
 * <p>
 * 格式： Compress method + Decompressed length + Compressed length + Compressed
 * data
 * <p>
 * 如果Compress method为<code>Lz4Consts.COMPRESS_METHOD_NONE</code>，
 * 那么将没有Compressed length字段， 因为此时Decompressed length与Compressed length相等。
 * <p>
 * 数据以外的部分都将使用Varint进行编码。
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class Lz4BodyCompressor implements MessageBodyCompressor {

    private static final int DEFAULT_BLOCK_SIZE = 8192;

    private static final int ESTIMATED_LENGTH_PARAM = 3;

    private final ThreadLocal<ByteBuffer> compressedByteBuffers = new ThreadLocal<ByteBuffer>();

    private final int blockSize;

    private final LZ4Compressor compressor;

    public Lz4BodyCompressor(int blockSize, LZ4Compressor compressor) {
        if (blockSize < Lz4Consts.MIN_BLOCK_SIZE) {
            throw new IllegalArgumentException(
                    "blockSize must be >= " + Lz4Consts.MIN_BLOCK_SIZE + ", got " + blockSize);
        }
        if (blockSize > Lz4Consts.MAX_BLOCK_SIZE) {
            throw new IllegalArgumentException(
                    "blockSize must be <= " + Lz4Consts.MAX_BLOCK_SIZE + ", got " + blockSize);
        }
        this.blockSize = blockSize;
        this.compressor = compressor;
    }

    public Lz4BodyCompressor(int blockSize) {
        this(blockSize, LZ4Factory.fastestInstance().fastCompressor());
    }

    public Lz4BodyCompressor() {
        this(DEFAULT_BLOCK_SIZE);
    }

    @Override
    public ChannelBuffer compress(ChannelBuffer channelBuffer) {
        int bodyLen = channelBuffer.readableBytes();
        if (bodyLen == 0) {
            return channelBuffer;
        }
        int estimatedLength = bodyLen / ESTIMATED_LENGTH_PARAM;
        ChannelBuffer compressedChannelBuffer = ChannelBuffers.dynamicBuffer(estimatedLength);

        ByteBuffer byteBuffer = channelBuffer.toByteBuffer();
        ByteBuffer compressedByteBuffer = getCompressedByteBuffer();
        int pos = byteBuffer.position();
        int limit = byteBuffer.limit();
        while (pos < limit) {
            int srcLen = 0;
            int reamainLen = limit - pos;
            if (reamainLen >= blockSize) {
                srcLen = blockSize;
            } else {
                srcLen = reamainLen;
            }
            compress(byteBuffer, pos, srcLen, compressedByteBuffer, compressedChannelBuffer);
            compressedByteBuffer.clear();
            pos += srcLen;
        }

        return compressedChannelBuffer;
    }

    private void compress(ByteBuffer byteBuffer, int offset, int srcLen,
                          ByteBuffer compressedByteBuffer, ChannelBuffer outBuffer) {
        int compressedLength = compressor.compress(byteBuffer, offset, srcLen, compressedByteBuffer,
                0, compressedByteBuffer.capacity());
        final int compressMethod;
        if (compressedLength >= srcLen) {
            compressMethod = Lz4Consts.COMPRESS_METHOD_NONE;
            compressedLength = srcLen;
        } else {
            compressMethod = Lz4Consts.COMPRESS_METHOD_LZ4;
        }

        NettyUtils.writeVarint32(outBuffer, compressMethod); // compressMethod
        NettyUtils.writeVarint32(outBuffer, srcLen); // decompressed length
        if (compressMethod == Lz4Consts.COMPRESS_METHOD_LZ4) {
            NettyUtils.writeVarint32(outBuffer, compressedLength); // compressed length
            compressedByteBuffer.limit(compressedLength);
            outBuffer.writeBytes(compressedByteBuffer); // compressed data
        } else {
            byteBuffer.position(offset).limit(offset + srcLen);
            outBuffer.writeBytes(byteBuffer); // compressed data
        }
    }

    private ByteBuffer getCompressedByteBuffer() {
        ByteBuffer byteBuffer = compressedByteBuffers.get();
        if (byteBuffer == null) {
            byteBuffer = ByteBuffer.allocate(compressor.maxCompressedLength(blockSize));
            compressedByteBuffers.set(byteBuffer);
        }
        return byteBuffer;
    }

}
