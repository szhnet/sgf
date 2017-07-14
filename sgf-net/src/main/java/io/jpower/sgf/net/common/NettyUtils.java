package io.jpower.sgf.net.common;

import org.jboss.netty.buffer.ChannelBuffer;

import java.io.IOException;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class NettyUtils {

    public static void writeVarint32(ChannelBuffer channelBuffer, int value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
                channelBuffer.writeByte(value);
                return;
            } else {
                channelBuffer.writeByte((value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }

    public static int readRawVarint32(ChannelBuffer channelBuffer) {
        int result = 0;
        int shift = 0;
        for (; shift < 29; shift += 7) {
            final byte b = channelBuffer.readByte();
            result |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
        }
        long longResult = result;
        for (; shift < 64; shift += 7) {
            final byte b = channelBuffer.readByte();
            longResult |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return (int) longResult;
            }
        }
        throw new IllegalStateException("Encountered a malformed varint.");
    }

    public static void writeVarint64(ChannelBuffer channelBuffer, long value) {
        while (true) {
            if ((value & ~0x7FL) == 0) {
                channelBuffer.writeByte((int) value);
                return;
            } else {
                channelBuffer.writeByte(((int) value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }

    public static long readRawVarint64(ChannelBuffer channelBuffer) {
        long result = 0;
        for (int shift = 0; shift < 64; shift += 7) {
            final byte b = channelBuffer.readByte();
            result |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
        }
        throw new IllegalStateException("Encountered a malformed varint.");
    }

}
