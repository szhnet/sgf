package io.jpower.sgf.net;

import io.jpower.sgf.net.compress.MessageBodyCompressor;
import io.jpower.sgf.net.msg.MessageConfig;
import io.jpower.sgf.net.msg.MessageConfigManager;
import io.jpower.sgf.net.msg.NetMessage;
import io.jpower.sgf.net.msg.ShareChannelMessage;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jpower.sgf.net.codec.MessageBodyEncoder;
import io.jpower.sgf.net.msg.MessageConfig.MessageMeta;

/**
 * {@link NetMessage}消息编码器
 *
 * @author zheng.sun
 */
public class NetMessageEncoder extends OneToOneEncoder {

    private static final Logger log = LoggerFactory.getLogger(NetMessageEncoder.class);

    private static final int DEFUALT_COMPRESS_THRESHOLD = 4096;

    private MessageBodyEncoder bodyEncoder;

    private MessageBodyCompressor bodyCompressor;

    private int compressThreshold;

    private MessageConfigManager<MessageMeta> messageConfigManager;

    private NetConfig netConfig;

    public NetMessageEncoder(NetConfig netConfig,
                             MessageConfigManager<MessageConfig.MessageMeta> messageConfigManager,
                             MessageBodyEncoder bodyEncoder) {
        this(netConfig, messageConfigManager, bodyEncoder, null);
    }

    public NetMessageEncoder(NetConfig netConfig,
                             MessageConfigManager<MessageConfig.MessageMeta> messageConfigManager, MessageBodyEncoder bodyEncoder,
                             MessageBodyCompressor bodyCompressor) {
        this(netConfig, messageConfigManager, bodyEncoder, bodyCompressor,
                DEFUALT_COMPRESS_THRESHOLD);
    }

    public NetMessageEncoder(NetConfig netConfig,
                             MessageConfigManager<MessageConfig.MessageMeta> messageConfigManager, MessageBodyEncoder bodyEncoder,
                             MessageBodyCompressor bodyCompressor, int compressThreshold) {
        this.netConfig = netConfig;
        this.messageConfigManager = messageConfigManager;
        this.bodyEncoder = bodyEncoder;
        this.bodyCompressor = bodyCompressor;
        if (bodyCompressor != null) {
            this.compressThreshold = compressThreshold;
        } else {
            this.compressThreshold = -1;
        }
    }

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg)
            throws Exception {
        if (netConfig.isShareChannel()) {
            return shareChannelEncode(ctx, channel, msg);
        } else {
            return defaultEncode(ctx, channel, msg);
        }
    }

    private Object defaultEncode(ChannelHandlerContext ctx, Channel channel, Object msg)
            throws Exception {
        int msgType = 0;
        Object body = null;
        if (bodyEncoder.canEncode(msg)) {
            body = msg;
            msgType = messageConfigManager.getMessageType(body.getClass());
            if (msgType < 0) {
                log.error("Not found message Type mapping: " + body.getClass().getName());
                return null; // 编码失败
            }
        } else {
            return msg; // 不支持的类型，直接原样返回，调用层会继续发个下一个ChannelHandler，具体见OneToOneEncoder源码
        }

        boolean enableSequenceMode = netConfig.isEnableSequenceMode();

        NetSession session = (NetSession) channel.getAttachment();
        int headerSize = NetMessage.HEADER_SIZE;
        if (enableSequenceMode) {
            headerSize += NetMessage.SEQUENCE_SIZE;
        }
        // ChannelBuffer encodeBuffer =
        // ChannelBuffers.dynamicBuffer(session.getEncodeBufferSize());
        ChannelBuffer headerBuffer = ChannelBuffers.buffer(headerSize);
        int flag = 0;
        headerBuffer.writeShort(0); // flag 占位
        if (enableSequenceMode) {
            headerBuffer.writeShort(session.nextSendSequence());
        }
        headerBuffer.writeShort(msgType);
        int bodyLenIdx = headerBuffer.writerIndex();
        headerBuffer.writeInt(0); // bodyLen 占位

        // encode
        ChannelBuffer bodyBuffer = ChannelBuffers.dynamicBuffer(session.getEncodeBufferSize());
        bodyEncoder.encode(body, bodyBuffer);
        int bodyLen = bodyBuffer.readableBytes();
        if (bodyCompressor != null && bodyLen >= compressThreshold) {
            bodyBuffer = bodyCompressor.compress(bodyBuffer);
            int srcBodyLen = bodyLen;
            bodyLen = bodyBuffer.readableBytes();
            flag = NetMessageHelper.createFlag(flag, true);

            if (log.isDebugEnabled()) {
                log.debug("compress {} {} -> {}", msgType + "-" + body.getClass().getSimpleName(),
                        srcBodyLen, bodyLen);
            }
        }

        headerBuffer.setShort(0, flag); // flag
        headerBuffer.setInt(bodyLenIdx, bodyLen); // bodyLen

        ChannelBuffer msgBuffer = ChannelBuffers.wrappedBuffer(headerBuffer, bodyBuffer);

        if (log.isDebugEnabled()) {
            log.debug("-->>{}: {}", NetHelper.getSessionDesc(session),
                    msgType + "-" + body.getClass().getSimpleName());

            log.debug("MessageBody: " + body);
        }

        return msgBuffer;
    }

    private Object shareChannelEncode(ChannelHandlerContext ctx, Channel channel, Object o)
            throws Exception {
        ShareChannelMessageWrapper smsg = (ShareChannelMessageWrapper) o;
        Object msg = smsg.getMessage();
        int msgType = 0;
        Object body = null;
        if (bodyEncoder.canEncode(msg)) {
            body = msg;
            msgType = messageConfigManager.getMessageType(body.getClass());
            if (msgType < 0) {
                log.error("Not found message Type mapping: " + body.getClass().getName());
                return null; // 编码失败
            }
        } else {
            return o; // 不支持的类型，直接原样返回，调用层会继续发个下一个ChannelHandler，具体见OneToOneEncoder源码
        }

        boolean enableSequenceMode = netConfig.isEnableSequenceMode();

        ShareChannelSession session = (ShareChannelSession) smsg.getSession();
        int headerSize = ShareChannelMessage.HEADER_SIZE;
        ChannelBuffer encodeBuffer = ChannelBuffers.dynamicBuffer(session.getEncodeBufferSize());
        encodeBuffer.writeShort(0); // flag
        if (enableSequenceMode) {
            encodeBuffer.writeShort(session.nextSendSequence());
            headerSize += NetMessage.SEQUENCE_SIZE;
        }
        encodeBuffer.writeShort(msgType);
        int bodyLenIdx = encodeBuffer.writerIndex();
        encodeBuffer.writeInt(0); // 先占一下位
        encodeBuffer.writeInt(session.getId()); // sid

        int msgLen, bodyLen;
        bodyEncoder.encode(body, encodeBuffer);
        msgLen = encodeBuffer.readableBytes();
        bodyLen = msgLen - headerSize;
        encodeBuffer.setInt(bodyLenIdx, bodyLen);

        if (log.isDebugEnabled()) {
            log.debug("-->>{}: {}", NetHelper.getSessionDesc(session),
                    msgType + "-" + body.getClass().getSimpleName());

            log.debug("MessageBody: " + body);
        }

        return encodeBuffer;
    }

}
