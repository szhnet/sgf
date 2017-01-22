package io.jpower.sgf.net.codec.thrift;

import io.jpower.sgf.net.codec.MessageBodyDecoder;
import io.jpower.sgf.net.msg.MessageConfig;
import io.jpower.sgf.net.msg.MessageConfigManager;
import org.apache.thrift.TBase;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.jboss.netty.buffer.ChannelBuffer;

import io.jpower.sgf.net.common.NettyChannelBufferReadTransport;
import io.jpower.sgf.net.msg.MessageConfig.MessageMeta;
import io.jpower.sgf.thread.Sharable;

/**
 * <a href=https://thrift.apache.org/>Thrift</a>消息体解码器
 * <p>
 * <li>默认使用{@link TCompactProtocol}协议格式</li>
 *
 * @author zheng.sun
 */
@Sharable
public class ThriftBodyDecoder implements MessageBodyDecoder {

    private static final TProtocolFactory DEFAULT_PROTOCOL_FACTORY = new TCompactProtocol.Factory(
            512, 512);

    private final ThreadLocal<TProtocol> protocols = new ThreadLocal<TProtocol>();

    private TProtocolFactory protocolFactory = DEFAULT_PROTOCOL_FACTORY;

    private MessageConfigManager<MessageMeta> messageConfigManager;

    public ThriftBodyDecoder() {

    }

    public ThriftBodyDecoder(TProtocolFactory protocolFactory,
                             MessageConfigManager<MessageConfig.MessageMeta> messageConfigManager) {
        setProtocolFactory(protocolFactory);
        setMessageConfigManager(messageConfigManager);
    }

    public void setMessageConfigManager(MessageConfigManager<MessageConfig.MessageMeta> messageConfigManager) {
        if (messageConfigManager == null) {
            throw new NullPointerException("messageConfigManager");
        }
        if (this.messageConfigManager != null) {
            throw new IllegalStateException("messageConfigManager can't change once set.");
        }
        this.messageConfigManager = messageConfigManager;
    }

    public void setProtocolFactory(TProtocolFactory protocolFactory) {
        if (protocolFactory == null) {
            throw new NullPointerException("protocolFactory");
        }
        if (this.protocolFactory != DEFAULT_PROTOCOL_FACTORY) {
            throw new IllegalStateException("protocolFactory can't change once set.");
        }
        this.protocolFactory = protocolFactory;
    }

    @Override
    public Object decode(int msgType, ChannelBuffer buffer, int bodyLength) throws Exception {
        Class<TBase<?, ?>> msgBodyClass = messageConfigManager.getMessageBodyClass(msgType);
        if (msgBodyClass == null) {
            throw new IllegalArgumentException("Not found message Type mapping: " + msgType);
        }

        msgBodyClass.getConstructor();

        TProtocol protocol = getTProtocol();
        NettyChannelBufferReadTransport trans = (NettyChannelBufferReadTransport) protocol
                .getTransport();

        trans.setInputBuffer(buffer); // 设置buffer
        try {
            TBase<?, ?> msgBody = msgBodyClass.newInstance();
            msgBody.read(protocol);
            return msgBody;
        } finally {
            trans.setInputBuffer(null); // 清除之前设置的buffer
        }
    }

    private TProtocol getTProtocol() {
        TProtocol protocol = protocols.get();
        if (protocol == null) {
            protocol = protocolFactory.getProtocol(new NettyChannelBufferReadTransport());
            protocols.set(protocol);
        }
        return protocol;
    }

}
