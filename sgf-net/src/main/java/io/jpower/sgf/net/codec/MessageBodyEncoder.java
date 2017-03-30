package io.jpower.sgf.net.codec;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * 消息体编码器
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public interface MessageBodyEncoder {

    /**
     * 能否对此对象进行编码
     *
     * @param msg
     * @return
     */
    boolean canEncode(Object msg);

    /**
     * 进行编码
     *
     * @param body          消息体
     * @param channelBuffer 用来存储编码字节的buffer
     * @throws Exception
     */
    void encode(Object body, ChannelBuffer channelBuffer) throws Exception;

}
