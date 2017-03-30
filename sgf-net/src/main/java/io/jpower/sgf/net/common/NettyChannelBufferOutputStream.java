package io.jpower.sgf.net.common;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * 将Netty的Channel Buffer包装成一个OutputStream
 * <p>
 * <ul>
 * <li>修改自org.jboss.netty.buffer.ChannelBufferOutputStream</li>
 * <li>支持对内部buffer的替换</li>
 * </ul>
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class NettyChannelBufferOutputStream extends OutputStream implements DataOutput {

    private ChannelBuffer buffer;

    private int startIndex;

    private final DataOutputStream utf8out = new DataOutputStream(this);

    public NettyChannelBufferOutputStream() {

    }

    /**
     * Creates a new stream which writes data to the specified {@code buffer}.
     */
    public NettyChannelBufferOutputStream(ChannelBuffer buffer) {
        buffer(buffer);
    }

    public void buffer(ChannelBuffer buffer) {
        if (buffer == null) {
            throw new NullPointerException("buffer");
        }
        this.buffer = buffer;
        this.startIndex = buffer.writerIndex();
    }

    public void clearBuffer() {
        this.buffer = null;
        this.startIndex = 0;
    }

    /**
     * Returns the buffer where this stream is writing data.
     */
    public ChannelBuffer buffer() {
        return buffer;
    }

    /**
     * Returns the number of written bytes by this stream so far.
     */
    public int writtenBytes() {
        return buffer.writerIndex() - startIndex;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }

        buffer.writeBytes(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        buffer.writeBytes(b);
    }

    @Override
    public void write(int b) throws IOException {
        buffer.writeByte((byte) b);
    }

    public void writeBoolean(boolean v) throws IOException {
        write(v ? (byte) 1 : (byte) 0);
    }

    public void writeByte(int v) throws IOException {
        write(v);
    }

    public void writeBytes(String s) throws IOException {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            write((byte) s.charAt(i));
        }
    }

    public void writeChar(int v) throws IOException {
        writeShort(v);
    }

    public void writeChars(String s) throws IOException {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            writeChar(s.charAt(i));
        }
    }

    public void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    public void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    public void writeInt(int v) throws IOException {
        buffer.writeInt(v);
    }

    public void writeLong(long v) throws IOException {
        buffer.writeLong(v);
    }

    public void writeShort(int v) throws IOException {
        buffer.writeShort((short) v);
    }

    public void writeUTF(String s) throws IOException {
        utf8out.writeUTF(s);
    }

}
