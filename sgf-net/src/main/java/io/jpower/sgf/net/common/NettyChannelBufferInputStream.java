package io.jpower.sgf.net.common;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * 将Netty的Channel Buffer包装成一个InputStream
 * <p>
 * <ul>
 * <li>修改自org.jboss.netty.buffer.ChannelBufferInputStream</li>
 * <li>支持对内部buffer的替换</li>
 * </ul>
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class NettyChannelBufferInputStream extends InputStream implements DataInput {

    private ChannelBuffer buffer;

    private int startIndex;

    private int endIndex;

    private int markedIndex;

    public NettyChannelBufferInputStream() {

    }

    public NettyChannelBufferInputStream(ChannelBuffer buffer) {
        this(buffer, buffer.readableBytes());
    }

    public NettyChannelBufferInputStream(ChannelBuffer buffer, int length) {
        buffer(buffer, length);
    }

    public void buffer(ChannelBuffer buffer) {
        buffer(buffer, buffer.readableBytes());
    }

    public void buffer(ChannelBuffer buffer, int length) {
        if (buffer == null) {
            throw new NullPointerException("buffer");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length: " + length);
        }
        if (length > buffer.readableBytes()) {
            throw new IndexOutOfBoundsException("Too many bytes to be read - Needs " + length
                    + ", maximum is " + buffer.readableBytes());
        }

        this.buffer = buffer;
        startIndex = buffer.readerIndex();
        endIndex = startIndex + length;
    }

    public void clearBuffer() {
        this.buffer = null;
        this.startIndex = 0;
        this.endIndex = 0;
        this.markedIndex = 0;
        if (this.lineBuf.capacity() >= 8 * 1024) {
            this.lineBuf = new StringBuilder();
        }
    }

    public ChannelBuffer buffer() {
        return buffer;
    }

    /**
     * Returns the number of read bytes by this stream so far.
     */
    public int readBytes() {
        return buffer.readerIndex() - startIndex;
    }

    @Override
    public int available() throws IOException {
        return endIndex - buffer.readerIndex();
    }

    @Override
    public void mark(int readlimit) {
        this.markedIndex = buffer.readerIndex();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read() throws IOException {
        if (!buffer.readable()) {
            return -1;
        }
        return buffer.readByte() & 0xff;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int available = available();
        if (available == 0) {
            return -1;
        }

        len = Math.min(available, len);
        buffer.readBytes(b, off, len);
        return len;
    }

    @Override
    public void reset() throws IOException {
        buffer.readerIndex(markedIndex);
    }

    @Override
    public long skip(long n) throws IOException {
        if (n > Integer.MAX_VALUE) {
            return skipBytes(Integer.MAX_VALUE);
        } else {
            return skipBytes((int) n);
        }
    }

    public boolean readBoolean() throws IOException {
        checkAvailable(1);
        return read() != 0;
    }

    public byte readByte() throws IOException {
        if (!buffer.readable()) {
            throw new EOFException();
        }
        return buffer.readByte();
    }

    public char readChar() throws IOException {
        return (char) readShort();
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        checkAvailable(len);
        buffer.readBytes(b, off, len);
    }

    public int readInt() throws IOException {
        checkAvailable(4);
        return buffer.readInt();
    }

    private StringBuilder lineBuf = new StringBuilder();

    public String readLine() throws IOException {
        lineBuf.setLength(0);
        for (; ; ) {
            int b = read();
            if (b < 0 || b == '\n') {
                break;
            }

            lineBuf.append((char) b);
        }

        if (lineBuf.length() > 0) {
            while (lineBuf.charAt(lineBuf.length() - 1) == '\r') {
                lineBuf.setLength(lineBuf.length() - 1);
            }
        }

        return lineBuf.toString();
    }

    public long readLong() throws IOException {
        checkAvailable(8);
        return buffer.readLong();
    }

    public short readShort() throws IOException {
        checkAvailable(2);
        return buffer.readShort();
    }

    public String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }

    public int readUnsignedByte() throws IOException {
        return readByte() & 0xff;
    }

    public int readUnsignedShort() throws IOException {
        return readShort() & 0xffff;
    }

    public int skipBytes(int n) throws IOException {
        int nBytes = Math.min(available(), n);
        buffer.skipBytes(nBytes);
        return nBytes;
    }

    private void checkAvailable(int fieldSize) throws IOException {
        if (fieldSize < 0) {
            throw new IndexOutOfBoundsException("fieldSize cannot be a negative number");
        }
        if (fieldSize > available()) {
            throw new EOFException("fieldSize is too long! Length is " + fieldSize
                    + ", but maximum is " + available());
        }
    }

}
