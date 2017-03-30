package io.jpower.sgf.net.qp;

import io.jpower.sgf.net.NetConfig;
import io.jpower.sgf.net.NetHelper;
import io.jpower.sgf.net.NetMessageHelper;
import io.jpower.sgf.net.ShareChannelSession;
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

import io.jpower.sgf.net.NetMessageEncoder;
import io.jpower.sgf.net.NetSession;
import io.jpower.sgf.net.ShareChannelMessageWrapper;
import io.jpower.sgf.net.codec.MessageBodyEncoder;
import io.jpower.sgf.net.msg.MessageConfig.MessageMeta;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class NetQpMessageEncoder extends OneToOneEncoder {

    private static final Logger log = LoggerFactory.getLogger(NetMessageEncoder.class);

    private MessageBodyEncoder bodyEncoder;

    private MessageConfigManager<MessageMeta> messageConfigManager;

    private NetConfig netConfig;

    public NetQpMessageEncoder(NetConfig netConfig, MessageBodyEncoder bodyEncoder,
                               MessageConfigManager<MessageConfig.MessageMeta> messageConfigManager) {
        this.netConfig = netConfig;
        this.bodyEncoder = bodyEncoder;
        this.messageConfigManager = messageConfigManager;
    }

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object o) throws Exception {
        if (netConfig.isShareChannel()) {
            return shareChannelEncode(ctx, channel, o);
        } else {
            return defaultEncode(ctx, channel, o);
        }
    }

    private Object defaultEncode(ChannelHandlerContext ctx, Channel channel, Object o)
            throws Exception {
        int msgType = 0;
        Object body = null;
        boolean enableRequestMode = netConfig.isEnableRequestMode();
        Object msg = o;
        int requestId = -1;
        int requestMode = NetMessage.QP_NONE;

        if (enableRequestMode) {
            if (msg instanceof RequestContext<?, ?>) {
                RequestContext<?, ?> q = (RequestContext<?, ?>) o;
                msg = q.getRequestMessage();
                requestId = q.getRequestId();
                requestMode = NetMessage.QP_REQUEST;
            } else if (msg instanceof Response) {
                Response<?> p = (Response<?>) o;
                msg = p.getMessage();
                requestId = p.getRequestId();
                requestMode = NetMessage.QP_RESPONSE;
            }
        }
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

        NetSession session = (NetSession) channel.getAttachment();
        ChannelBuffer encodeBuffer = ChannelBuffers.dynamicBuffer(session.getEncodeBufferSize());
        encodeBuffer.writeShort(NetMessageHelper.createRequestModeFlag(0, requestMode));
        encodeBuffer.writeShort(msgType);
        int bodyLenIdx = encodeBuffer.writerIndex();
        encodeBuffer.writeInt(0); // 先占一下位
        if (requestMode != NetMessage.QP_NONE) {
            encodeBuffer.writeInt(requestId);
        }

        int msgLen, bodyLen;
        bodyEncoder.encode(body, encodeBuffer);
        msgLen = encodeBuffer.readableBytes();
        bodyLen = msgLen - NetMessage.HEADER_SIZE;
        if (requestMode != NetMessage.QP_NONE) {
            bodyLen -= NetMessage.REQUEST_ID_SIZE;
        }
        encodeBuffer.setInt(bodyLenIdx, bodyLen);

        if (log.isDebugEnabled()) {
            log.debug("-->>{}: {}", NetHelper.getSessionDesc(session),
                    msgType + "-" + body.getClass().getSimpleName());

            log.debug("MessageBody: " + body);
        }

        return encodeBuffer;
    }

    private Object shareChannelEncode(ChannelHandlerContext ctx, Channel channel, Object o)
            throws Exception {
        ShareChannelMessageWrapper smsg = (ShareChannelMessageWrapper) o;
        int msgType = 0;
        Object body = null;
        boolean enableRequestMode = netConfig.isEnableRequestMode();
        Object msg = smsg.getMessage();
        int requestId = -1;
        int requestMode = NetMessage.QP_NONE;

        if (enableRequestMode) {
            if (msg instanceof RequestContext<?, ?>) {
                RequestContext<?, ?> q = (RequestContext<?, ?>) msg;
                msg = q.getRequestMessage();
                requestId = q.getRequestId();
                requestMode = NetMessage.QP_REQUEST;
            } else if (msg instanceof Response) {
                Response<?> p = (Response<?>) msg;
                msg = p.getMessage();
                requestId = p.getRequestId();
                requestMode = NetMessage.QP_RESPONSE;
            }
        }
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

        ShareChannelSession session = smsg.getSession();
        ChannelBuffer encodeBuffer = ChannelBuffers.dynamicBuffer(session.getEncodeBufferSize());
        encodeBuffer.writeShort(NetMessageHelper.createRequestModeFlag(0, requestMode));
        encodeBuffer.writeShort(msgType);
        int bodyLenIdx = encodeBuffer.writerIndex();
        encodeBuffer.writeInt(0); // 先占一下位
        encodeBuffer.writeInt(session.getId()); // sid
        if (requestMode != NetMessage.QP_NONE) {
            encodeBuffer.writeInt(requestId);
        }

        int msgLen, bodyLen;
        bodyEncoder.encode(body, encodeBuffer);
        msgLen = encodeBuffer.readableBytes();
        bodyLen = msgLen - ShareChannelMessage.HEADER_SIZE;
        if (requestMode != NetMessage.QP_NONE) {
            bodyLen -= NetMessage.REQUEST_ID_SIZE;
        }
        encodeBuffer.setInt(bodyLenIdx, bodyLen);

        if (log.isDebugEnabled()) {
            log.debug("-->>{}: {}", NetHelper.getSessionDesc(session),
                    msgType + "-" + body.getClass().getSimpleName());

            log.debug("MessageBody: " + body);
        }

        return encodeBuffer;
    }

}
