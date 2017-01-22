package io.jpower.sgf.net.common;

import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * 内部使用netty的ChannelBuffer实现的TTransport
 * <p>
 * <p>
 * 用来使用netty搭配thrift进行编解码
 *
 * @author zheng.sun
 */
public class NettyChannelBufferReadTransport extends TTransport {

    private ChannelBuffer inputBuffer;

    @Override
    public boolean isOpen() {
        // Buffer is always open
        return true;
    }

    @Override
    public void open() throws TTransportException {
        // Buffer is always open
    }

    @Override
    public void close() {
        // Buffer is always open
    }

    @Override
    public int read(byte[] buf, int off, int len) throws TTransportException {
        int readableBytes = inputBuffer.readableBytes();
        int bytesToRead = len > readableBytes ? readableBytes : len;
        if (bytesToRead > 0) {
            inputBuffer.readBytes(buf, off, bytesToRead);
        }
        return bytesToRead;
    }

    @Override
    public void write(byte[] buf, int off, int len) throws TTransportException {
        throw new UnsupportedOperationException();
    }

    public ChannelBuffer getInputBuffer() {
        return inputBuffer;
    }

    /**
     * 设置用来进行读取操作的ChannelBuffer
     *
     * @param inputBuffer
     */
    public void setInputBuffer(ChannelBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    @Override
    public byte[] getBuffer() {
        if (inputBuffer.hasArray()) {
            return inputBuffer.array();
        } else {
            return null;
        }
    }

    @Override
    public int getBufferPosition() {
        if (inputBuffer.hasArray()) {
            return inputBuffer.readerIndex() + inputBuffer.arrayOffset();
        } else {
            return 0;
        }
    }

    @Override
    public int getBytesRemainingInBuffer() {
        if (inputBuffer.hasArray()) {
            return inputBuffer.readableBytes();
        } else {
            return -1;
        }
    }

    @Override
    public void consumeBuffer(int len) {
        if (inputBuffer.hasArray()) {
            inputBuffer.readerIndex(inputBuffer.readerIndex() + len);
        }
    }

}
