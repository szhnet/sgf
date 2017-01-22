package io.jpower.sgf.net.handler;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用来处理Flash pollicy请求
 *
 * @author zheng.sun
 */
public class FlashPolicyHandler extends FrameDecoder {

    private static final Logger log = LoggerFactory.getLogger(FlashPolicyHandler.class);

    /**
     * flash的policy请求内容，这个是固定的
     */
    private static final String POLICY_REQ_STR = "<policy-file-request/>\0";

    private static final ChannelBuffer POLICY_REQ_BUFFER;

    /**
     * 响应的数据，也是固定的
     */
    private static final String POLICY_RESP_STR = "<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"*\" /></cross-domain-policy>";

    private static final byte[] POLICY_RESP_BYTES;

    static {
        try {
            POLICY_REQ_BUFFER = ChannelBuffers.wrappedBuffer(POLICY_REQ_STR.getBytes("UTF-8"));
            POLICY_REQ_BUFFER.writerIndex(POLICY_REQ_BUFFER.capacity());
            POLICY_RESP_BYTES = POLICY_RESP_STR.getBytes("UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer)
            throws Exception {
        // 先判断前两个字节
        if (buffer.readableBytes() < 2) {
            return null;
        }
        final short magic1 = buffer.getUnsignedByte(buffer.readerIndex());
        final short magic2 = buffer.getUnsignedByte(buffer.readerIndex() + 1);
        boolean isFlashPolicyRequest = (magic1 == '<' && magic2 == 'p'); // magic number 15472
        if (isFlashPolicyRequest) {
            // 判断是否policy请求
            if (buffer.readableBytes() < POLICY_REQ_BUFFER.readableBytes()) {
                return null; // 数据还没读够
            }
            if (buffer.readableBytes() == POLICY_REQ_BUFFER.readableBytes()) {
                if (ChannelBuffers.equals(buffer, POLICY_REQ_BUFFER)) {
                    buffer.skipBytes(buffer.readableBytes()); // 跳过这些字节，相当于读取了
                    // 写policy数据
                    ChannelBuffer writeBuffer = ChannelBuffers.wrappedBuffer(POLICY_RESP_BYTES);
                    writeBuffer.writerIndex(writeBuffer.capacity());
                    channel.write(writeBuffer).addListener(ChannelFutureListener.CLOSE); // 加入监听，写后关闭
                    return null;
                }
            }
            // 数据内容不符，错误
            receiveWrongMsg(channel, buffer);
            return null;
        } else {
            // 已不需要处理policy，移除handler
            ctx.getPipeline().remove(this);
            return buffer.readBytes(buffer.readableBytes()); // 把数据传递给下一个handler处理
        }
    }

    private void receiveWrongMsg(Channel channel, ChannelBuffer buf) {
        if (log.isErrorEnabled()) {
            log.error("Flash Policy error. channel={}, buf={}", channel,
                    ChannelBuffers.hexDump(buf));
        }
        channel.close();
    }

}
