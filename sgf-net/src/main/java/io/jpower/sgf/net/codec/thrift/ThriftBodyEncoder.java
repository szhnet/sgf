package io.jpower.sgf.net.codec.thrift;

import org.apache.thrift.TBase;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.jboss.netty.buffer.ChannelBuffer;

import io.jpower.sgf.net.codec.MessageBodyEncoder;
import io.jpower.sgf.net.common.NettyChannelBufferWriteTransport;
import io.jpower.sgf.thread.Sharable;

/**
 * <a href=https://thrift.apache.org/>Thrift</a>消息体编码器
 * <p>
 * <li>默认使用{@link TCompactProtocol}协议格式</li>
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
@Sharable
public class ThriftBodyEncoder implements MessageBodyEncoder {

    private final ThreadLocal<TProtocol> protocols = new ThreadLocal<TProtocol>();

    private static final TProtocolFactory DEFAULT_PROTOCOL_FACTORY = new TCompactProtocol.Factory();

    private TProtocolFactory protocolFactory = DEFAULT_PROTOCOL_FACTORY;

    public ThriftBodyEncoder() {

    }

    public ThriftBodyEncoder(TProtocolFactory protocolFactory) {
        setProtocolFactory(protocolFactory);
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
    public boolean canEncode(Object msg) {
        return msg instanceof TBase;
    }

    @Override
    public void encode(Object body, ChannelBuffer channelBuffer) throws Exception {
        TBase<?, ?> tbody = (TBase<?, ?>) body;

        TProtocol protocol = getTProtocol();
        NettyChannelBufferWriteTransport trans = (NettyChannelBufferWriteTransport) protocol
                .getTransport();

        trans.setOutputBuffer(channelBuffer); // 设置buffer
        try {
            tbody.write(protocol);
        } finally {
            trans.setOutputBuffer(null); // 清除之前设置的buffer
        }

    }

    private TProtocol getTProtocol() {
        TProtocol protocol = protocols.get();
        if (protocol == null) {
            protocol = protocolFactory.getProtocol(new NettyChannelBufferWriteTransport());
            protocols.set(protocol);
        }

        return protocol;
    }

}
