package io.jpower.sgf.net;

import io.jpower.sgf.net.codec.MessageBodyEncoder;
import io.jpower.sgf.net.msg.MessageConfig;
import io.jpower.sgf.net.msg.MessageConfigManager;
import io.jpower.sgf.net.msg.ShareChannelMessage;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jpower.sgf.net.msg.MessageConfig.MessageMeta;

/**
 * @author zheng.sun
 */
public class ShareChannelTransferInboundEncoder extends OneToOneEncoder {

    private static final Logger log = LoggerFactory
            .getLogger(ShareChannelTransferInboundEncoder.class);

    private MessageBodyEncoder bodyEncoder;

    private MessageConfigManager<MessageMeta> messageConfigManager;

    public ShareChannelTransferInboundEncoder(MessageBodyEncoder bodyEncoder,
                                              MessageConfigManager<MessageConfig.MessageMeta> messageConfigManager) {
        this.bodyEncoder = bodyEncoder;
        this.messageConfigManager = messageConfigManager;
    }

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object o) throws Exception {
        // 这里暂不支持sequence，有需求再说，如果支持，需要区分netConfig（针对客户端netConfig和针对服务器的netConfig）
        if (o instanceof ShareChannelMessage) {
            ShareChannelMessage msg = (ShareChannelMessage) o;
            ChannelBuffer msgBodyBuffer = (ChannelBuffer) msg.getBody();

            NetSession session = (NetSession) channel.getAttachment();
            ChannelBuffer headBuffer = ChannelBuffers.buffer(ShareChannelMessage.HEADER_SIZE);
            headBuffer.writeShort(msg.getFlag());
            headBuffer.writeShort(msg.getType());
            headBuffer.writeInt(msgBodyBuffer.readableBytes());
            headBuffer.writeInt(msg.getSessionId()); // sid

            ChannelBuffer encodeBuffer = ChannelBuffers.wrappedBuffer(headBuffer, msgBodyBuffer);

            if (log.isDebugEnabled()) {
                log.debug("-->>{} {}: {}", NetHelper.getSessionDesc(session), msg.getSessionId(),
                        msg.getType());
            }

            return encodeBuffer;
        } else {
            int msgType = 0;
            Object body = null;
            if (bodyEncoder.canEncode(o)) {
                body = o;
                msgType = messageConfigManager.getMessageType(body.getClass());
                if (msgType < 0) {
                    log.error("Not found message Type mapping: " + body.getClass().getName());
                    return null; // 编码失败
                }
            } else {
                return o; // 不支持的类型，直接原样返回，调用层会继续发个下一个ChannelHandler，具体见OneToOneEncoder源码
            }

            NetSession session = (NetSession) channel.getAttachment();
            ChannelBuffer encodeBuffer = ChannelBuffers
                    .dynamicBuffer(session.getEncodeBufferSize());
            encodeBuffer.writeShort(0);
            encodeBuffer.writeShort(msgType);
            int bodyLenIdx = encodeBuffer.writerIndex();
            encodeBuffer.writeInt(0); // 先占一下位
            encodeBuffer.writeInt(-1); // 这种情况下没有sid，直接设置-1即可

            int msgLen, bodyLen;
            bodyEncoder.encode(body, encodeBuffer);
            msgLen = encodeBuffer.readableBytes();
            bodyLen = msgLen - ShareChannelMessage.FLAG_SIZE;
            encodeBuffer.setInt(bodyLenIdx, bodyLen);

            if (log.isDebugEnabled()) {
                log.debug("-->>{}: {}", NetHelper.getSessionDesc(session),
                        msgType + "-" + body.getClass().getSimpleName());

                log.debug("MessageBody: " + body);
            }

            return encodeBuffer;
        }
    }

}
