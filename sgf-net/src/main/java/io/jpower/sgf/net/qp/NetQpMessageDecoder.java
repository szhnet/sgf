package io.jpower.sgf.net.qp;

import io.jpower.sgf.net.NetConfig;
import io.jpower.sgf.net.NetMessageDecoder;
import io.jpower.sgf.net.NetMessageHelper;
import io.jpower.sgf.net.codec.MessageBodyDecoder;
import io.jpower.sgf.net.msg.NetMessage;
import io.jpower.sgf.net.msg.ShareChannelMessage;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jpower.sgf.net.NetSession;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class NetQpMessageDecoder extends FrameDecoder {

    private static final Logger log = LoggerFactory.getLogger(NetMessageDecoder.class);

    private MessageBodyDecoder bodyDecoder = null;

    /**
     * 消息体长度限制，超过此限制将记录error日志
     */
    private int errorBodyLength = 0;

    /**
     * 消息体长度限制，超过此限制将抛出异常
     */
    private int exceptionBodyLength = 0;

    private NetConfig netConfig;

    /**
     * @param netConfig
     * @param bodyDecoder
     */
    public NetQpMessageDecoder(NetConfig netConfig, MessageBodyDecoder bodyDecoder) {
        this(netConfig, bodyDecoder, 16 * 1024, 256 * 1024);
    }

    /**
     * @param netConfig           配置
     * @param bodyDecoder         消息体解码器
     * @param errorBodyLength     消息体长度限制，超过此限制将记录error日志
     * @param exceptionBodyLength 消息体长度限制，超过此限制将抛出异常
     */
    public NetQpMessageDecoder(NetConfig netConfig, MessageBodyDecoder bodyDecoder,
                               int errorBodyLength, int exceptionBodyLength) {
        this.netConfig = netConfig;
        this.bodyDecoder = bodyDecoder;
        this.errorBodyLength = errorBodyLength;
        this.exceptionBodyLength = exceptionBodyLength;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer)
            throws Exception {
        if (netConfig.isShareChannel()) {
            return shareChannelDecode(ctx, channel, buffer);
        } else {
            return defaultDecode(ctx, channel, buffer);
        }
    }

    private Object defaultDecode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer)
            throws Exception {
        boolean enableRequestMode = netConfig.isEnableRequestMode();
        if (buffer.readableBytes() < NetMessage.HEADER_SIZE) {
            return null;
        }

        int startReaderIdx = buffer.readerIndex(); // 标记起始位置，这里没有使用markReaderIndex和resetReaderIndex方法，感觉这样兼容性会更好一些，因为其他模块也有可能会调用这些方法，比如bodyDecoder的实现，那样会把index搞乱。
        NetSession session = (NetSession) channel.getAttachment();
        try {
            short flag = buffer.readShort();
            int requestMode = NetMessage.QP_NONE;
            if (enableRequestMode) {
                requestMode = NetMessageHelper.getRequestMode(flag);
            }
            if (requestMode != NetMessage.QP_NONE
                    && buffer.readableBytes() < NetMessage.HEADER_SIZE + NetMessage.REQUEST_ID_SIZE
                    - NetMessage.FLAG_SIZE /* 前面已经读取过flag了，所以这里要减去 */) {
                buffer.readerIndex(startReaderIdx); // 数据没到齐，恢复原来reader位置，继续收集
                return null;
            }
            int msgType = buffer.readUnsignedShort();
            int bodyLen = buffer.readInt();
            int requestId = requestMode != NetMessage.QP_NONE ? buffer.readInt() : -1;

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

            if (buffer.readableBytes() < bodyLen) {
                buffer.readerIndex(startReaderIdx); // 数据没到齐，恢复原来reader位置，继续收集
                return null;
            }

            NetMessage msg = new NetMessage();
            msg.setFlag(flag);
            msg.setType(msgType);
            msg.setRequestId(requestId);
            msg.setRequestMode(requestMode);

            Object msgBody = bodyDecoder.decode(msgType, buffer, bodyLen);
            msg.setBody(msgBody);

            return msg;
        } catch (Throwable e) {
            buffer.readerIndex(startReaderIdx); // 这里恢复reader的位置，是为了配合FrameDecoder的逻辑，当返回null且reader位置不变时，将停止解码剩下的数据（具体可以见FrameDecoder的源代码），因为下面将进行close，所以没必要解码剩下的数据了。
            log.error("Message decode error. session={}", session, e);
            channel.close(); // close
            return null;
        }
    }

    private Object shareChannelDecode(ChannelHandlerContext ctx, Channel channel,
                                      ChannelBuffer buffer) throws Exception {
        boolean enableRequestMode = netConfig.isEnableRequestMode();
        if (buffer.readableBytes() < NetMessage.HEADER_SIZE) {
            return null;
        }

        int startReaderIdx = buffer.readerIndex(); // 标记起始位置，这里没有使用markReaderIndex和resetReaderIndex方法，感觉这样兼容性会更好一些，因为其他模块也有可能会调用这些方法，比如bodyDecoder的实现，那样会把index搞乱。
        NetSession session = (NetSession) channel.getAttachment();
        try {
            short flag = buffer.readShort();
            int requestMode = NetMessage.QP_NONE;
            if (enableRequestMode) {
                requestMode = NetMessageHelper.getRequestMode(flag);
            }
            if (requestMode != NetMessage.QP_NONE
                    && buffer.readableBytes() < NetMessage.HEADER_SIZE + NetMessage.REQUEST_ID_SIZE
                    - NetMessage.FLAG_SIZE /* 前面已经读取过flag了，所以这里要减去 */) {
                buffer.readerIndex(startReaderIdx); // 数据没到齐，恢复原来reader位置，继续收集
                return null;
            }
            int msgType = buffer.readUnsignedShort();
            int bodyLen = buffer.readInt();
            int sid = buffer.readInt();
            int requestId = requestMode != NetMessage.QP_NONE ? buffer.readInt() : -1;

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

            ShareChannelMessage msg = new ShareChannelMessage();
            msg.setFlag(flag);
            msg.setType(msgType);
            msg.setSessionId(sid);
            msg.setRequestId(requestId);
            msg.setRequestMode(requestMode);

            Object msgBody = bodyDecoder.decode(msgType, buffer, bodyLen);
            msg.setBody(msgBody);

            return msg;
        } catch (Throwable e) {
            buffer.readerIndex(startReaderIdx); // 这里恢复reader的位置，是为了配合FrameDecoder的逻辑，当返回null且reader位置不变时，将停止解码剩下的数据（具体可以见FrameDecoder的源代码），因为下面将进行close，所以没必要解码剩下的数据了。
            log.error("Message decode error. session={}", session, e);
            channel.close(); // close
            return null;
        }
    }

}
