package io.jpower.sgf.net.codec;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * 消息体解码器
 *
 * @author zheng.sun
 */
public interface MessageBodyDecoder {

    /**
     * 进行解码
     *
     * @param msgType    消息类型
     * @param buffer     用来存储待解码字节的buffer
     * @param bodyLength 消息体长度
     * @return 解码之后的消息体
     * @throws Exception
     */
    Object decode(int msgType, ChannelBuffer buffer, int bodyLength) throws Exception;

}
