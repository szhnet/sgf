package io.jpower.sgf.net.common;

import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * 内部使用netty的ChannelBuffer实现的TTransport，用于写入
 * <p>
 * <p>
 * 用来使用netty搭配thrift进行编解码
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class NettyChannelBufferWriteTransport extends TTransport {

    private ChannelBuffer outputBuffer;

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
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(byte[] buf, int off, int len) throws TTransportException {
        outputBuffer.writeBytes(buf, off, len);
    }

    public ChannelBuffer getOutputBuffer() {
        return outputBuffer;
    }

    /**
     * 设置用来进行写入操作的ChannelBuffer
     *
     * @param outputBuffer
     */
    public void setOutputBuffer(ChannelBuffer outputBuffer) {
        this.outputBuffer = outputBuffer;
    }

}
