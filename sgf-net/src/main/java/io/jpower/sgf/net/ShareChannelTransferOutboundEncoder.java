package io.jpower.sgf.net;

import io.jpower.sgf.net.msg.NetMessage;
import io.jpower.sgf.net.msg.ShareChannelMessage;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class ShareChannelTransferOutboundEncoder extends OneToOneEncoder {

    private static final Logger log = LoggerFactory
            .getLogger(ShareChannelTransferOutboundEncoder.class);

    private NetConfig netConfig;

    public ShareChannelTransferOutboundEncoder(NetConfig netConfig) {
        this.netConfig = netConfig;
    }

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object o) throws Exception {
        if (!(o instanceof ShareChannelMessage)) {
            return o; // 不支持的类型，直接原样返回，调用层会继续发个下一个ChannelHandler，具体见OneToOneEncoder源码
        }

        ShareChannelMessage msg = (ShareChannelMessage) o;
        ChannelBuffer msgBodyBuffer = (ChannelBuffer) msg.getBody();

        boolean enableSequenceMode = netConfig.isEnableSequenceMode();

        NetSession session = (NetSession) channel.getAttachment();
        int headerSize = NetMessage.HEADER_SIZE;
        if (enableSequenceMode) {
            headerSize += NetMessage.SEQUENCE_SIZE;
        }
        ChannelBuffer headBuffer = ChannelBuffers.buffer(headerSize);
        headBuffer.writeShort(msg.getFlag());
        if (enableSequenceMode) {
            headBuffer.writeShort(session.nextSendSequence());
        }
        headBuffer.writeShort(msg.getType());
        headBuffer.writeInt(msgBodyBuffer.readableBytes());

        ChannelBuffer encodeBuffer = ChannelBuffers.wrappedBuffer(headBuffer, msgBodyBuffer);

        if (log.isDebugEnabled()) {
            log.debug("-->>{}: {}", NetHelper.getSessionDesc(session), msg.getType());
        }

        return encodeBuffer;
    }

}
