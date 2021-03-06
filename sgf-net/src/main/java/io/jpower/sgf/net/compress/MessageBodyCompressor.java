package io.jpower.sgf.net.compress;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * 消息体压缩器
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public interface MessageBodyCompressor {

    /**
     * 压缩
     *
     * @param channelBuffer
     * @return
     */
    ChannelBuffer compress(ChannelBuffer channelBuffer);

}
