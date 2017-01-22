package io.jpower.sgf.net;

import io.jpower.sgf.net.codec.MessageBodyDecoder;
import io.jpower.sgf.net.msg.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jpower.sgf.net.msg.MessageConfigManager;
import io.jpower.sgf.net.msg.MultiSessionMessage;
import io.jpower.sgf.net.msg.NetMessage;
import io.jpower.sgf.net.msg.ShareChannelMessage;
import io.jpower.sgf.net.msg.MessageConfig.MessageMeta;

/**
 * @author zheng.sun
 */
public class ShareChannelTransferOutboundDecoder extends FrameDecoder {

    private static final Logger log = LoggerFactory
            .getLogger(ShareChannelTransferOutboundDecoder.class);

    private MessageBodyDecoder bodyDecoder;

    private MessageConfigManager<MessageMeta> messageConfigManager;

    /**
     * 消息体长度限制，超过此限制将记录error日志
     */
    private int errorBodyLength = 0;

    /**
     * 消息体长度限制，超过此限制将抛出异常
     */
    private int exceptionBodyLength = 0;

    public ShareChannelTransferOutboundDecoder(MessageBodyDecoder bodyDecoder,
                                               MessageConfigManager<MessageConfig.MessageMeta> messageConfigManager) {
        this(bodyDecoder, messageConfigManager, 16 * 1024, 256 * 1024);
    }

    public ShareChannelTransferOutboundDecoder(MessageBodyDecoder bodyDecoder,
                                               MessageConfigManager<MessageConfig.MessageMeta> messageConfigManager, int errorBodyLength,
                                               int exceptionBodyLength) {
        this.bodyDecoder = bodyDecoder;
        this.messageConfigManager = messageConfigManager;
        this.errorBodyLength = errorBodyLength;
        this.exceptionBodyLength = exceptionBodyLength;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer)
            throws Exception {
        if (buffer.readableBytes() < ShareChannelMessage.HEADER_SIZE) {
            return null;
        }

        int startReaderIdx = buffer.readerIndex(); // 标记起始位置，这里没有使用markReaderIndex和resetReaderIndex方法，感觉这样兼容性会更好一些，因为其他模块也有可能会调用这些方法，比如bodyDecoder的实现，那样会把index搞乱。
        NetSession session = (NetSession) channel.getAttachment();
        try {
            short flag = buffer.readShort();
            boolean mutilSession = NetMessageHelper.isMutilSession(flag);
            if (mutilSession) {
                int bodyLen = buffer.getInt(buffer.readerIndex() - MultiSessionMessage.FLAG_SIZE
                        + MultiSessionMessage.HEADER_SIZE - MultiSessionMessage.SESSION_NUM_SIZE
                        - MultiSessionMessage.BODY_LENGTH_SIZE);
                int sessionNum = buffer.getUnsignedShort(buffer.readerIndex()
                        - MultiSessionMessage.FLAG_SIZE + MultiSessionMessage.HEADER_SIZE
                        - MultiSessionMessage.SESSION_NUM_SIZE); // session的数量
                if (sessionNum <= 0) {
                    throw new IllegalStateException("sessionNum <= 0");
                }
                int sessionIdsSize = sessionNum * MultiSessionMessage.SESSION_ID_SIZE;
                if (buffer.readableBytes() < MultiSessionMessage.HEADER_SIZE
                        - MultiSessionMessage.FLAG_SIZE + sessionIdsSize + bodyLen) {
                    buffer.readerIndex(startReaderIdx); // 数据没到齐，恢复原来reader位置，继续收集
                    return null;
                }
                int msgType = buffer.readUnsignedShort();
                bodyLen = buffer.readInt();
                if (bodyLen >= errorBodyLength) {
                    if (bodyLen >= exceptionBodyLength) {
                        throw new IllegalStateException(String.format(
                                "Message body length >= %d. session=%s, msgType=%d, length=%d",
                                exceptionBodyLength, session.toString(), msgType, bodyLen));
                    } else {
                        log.error(String.format(
                                "Message body length >= %d. session=%s, msgType=%d, length=%d",
                                exceptionBodyLength, session.toString(), msgType, bodyLen));
                    }
                }
                sessionNum = buffer.readUnsignedShort();

                int[] sessionIds = new int[sessionNum];
                for (int i = 0; i < sessionNum; i++) {
                    sessionIds[i] = buffer.readInt();
                }
                MultiSessionMessage msg = new MultiSessionMessage();
                msg.setFlag(flag);
                msg.setType(msgType);
                msg.setSeesionIds(sessionIds);

                ChannelBuffer msgBodyBuffer = buffer.slice(buffer.readerIndex(), bodyLen);
                buffer.skipBytes(bodyLen); // 移动buffer的reader指针
                msg.setBody(msgBodyBuffer);

                return msg;
            } else {
                int msgType = buffer.readUnsignedShort();
                int bodyLen = buffer.readInt();
                int sid = buffer.readInt();

                if (bodyLen >= errorBodyLength) {
                    if (bodyLen >= exceptionBodyLength) {
                        throw new IllegalStateException(String.format(
                                "Message body length >= %d. session=%s, sid=%d, msgType=%d, length=%d",
                                exceptionBodyLength, session.toString(), sid, msgType, bodyLen));
                    } else {
                        log.error(String.format(
                                "Message body length >= %d. session=%s, sid=%d, msgType=%d, length=%d",
                                exceptionBodyLength, session.toString(), sid, msgType, bodyLen));
                    }
                }

                if (buffer.readableBytes() < bodyLen) {
                    buffer.readerIndex(startReaderIdx); // 数据没到齐，恢复原来reader位置，继续收集
                    return null;
                }

                if (messageConfigManager.getMessageBodyClass(msgType) == null) { // session的数据消息
                    ShareChannelMessage msg = new ShareChannelMessage();
                    msg.setFlag(flag);
                    msg.setType(msgType);
                    msg.setSessionId(sid); // sid

                    ChannelBuffer msgBodyBuffer = buffer.slice(buffer.readerIndex(), bodyLen);
                    buffer.skipBytes(bodyLen); // 移动buffer的reader指针
                    msg.setBody(msgBodyBuffer);
                    return msg;
                } else { // 命令消息
                    NetMessage msg = new NetMessage();
                    msg.setFlag(flag);
                    msg.setType(msgType);

                    Object msgBody = bodyDecoder.decode(msgType, buffer, bodyLen);
                    msg.setBody(msgBody);
                    return msg;
                }
            }
        } catch (Throwable e) {
            buffer.readerIndex(startReaderIdx); // 这里恢复reader的位置，是为了配合FrameDecoder的逻辑，当返回null且reader位置不变时，将停止解码剩下的数据（具体可以见FrameDecoder的源代码），因为下面将进行close，所以没必要解码剩下的数据了。
            log.error("Message decode error. session={}", session, e);
            channel.close(); // close
            return null;
        }
    }

}
