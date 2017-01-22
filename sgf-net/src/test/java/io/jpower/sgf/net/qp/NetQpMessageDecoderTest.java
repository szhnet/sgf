package io.jpower.sgf.net.qp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.junit.Test;

import io.jpower.sgf.net.NetConfig;
import io.jpower.sgf.net.NetMessageHelper;
import io.jpower.sgf.net.NetSession;
import io.jpower.sgf.net.codec.MessageBodyDecoder;
import io.jpower.sgf.net.msg.CgMockLogin;
import io.jpower.sgf.net.msg.NetMessage;
import io.jpower.sgf.net.msg.ShareChannelMessage;

/**
 * @author zheng.sun
 */
public class NetQpMessageDecoderTest {

    @Test
    public void testDecodeRequest() throws Exception {
        int msgType = 1;

        ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
        int requestMode = NetMessage.QP_REQUEST;
        buf.writeShort(NetMessageHelper.createRequestModeFlag(0, requestMode)); // flag
        buf.writeShort(msgType);
        int bodyLen = 0;
        buf.writeInt(bodyLen);
        int requestId = 6;
        buf.writeInt(requestId);

        CgMockLogin msgBody = new CgMockLogin("101");

        MessageBodyDecoder bodyDecoder = createMock(MessageBodyDecoder.class);
        expect(bodyDecoder.decode(msgType, buf, bodyLen)).andReturn(msgBody);

        Channel channel = createMockChannel(1);

        replay(bodyDecoder);
        replay(channel);

        NetConfig netConfig = new NetConfig();
        netConfig.setEnableRequestMode(true);

        NetQpMessageDecoder decoder = new NetQpMessageDecoder(netConfig, bodyDecoder);
        NetMessage msg = (NetMessage) decoder.decode(createMock(ChannelHandlerContext.class),
                channel, buf);

        assertEquals(msgType, msg.getType());
        assertSame(msgBody, msg.getBody());
        assertEquals(requestId, msg.getRequestId());
        assertEquals(requestMode, msg.getRequestMode());

        verify(bodyDecoder);
        verify(channel);
    }

    @Test
    public void testShareSessionDecodeRequest() throws Exception {
        int msgType = 1;

        ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
        int requestMode = NetMessage.QP_REQUEST;
        buf.writeShort(NetMessageHelper.createRequestModeFlag(0, requestMode)); // flag
        buf.writeShort(msgType);
        int bodyLen = 0;
        buf.writeInt(bodyLen);
        int sid = 10;
        buf.writeInt(sid);
        int requestId = 6;
        buf.writeInt(requestId);

        CgMockLogin msgBody = new CgMockLogin("101");

        MessageBodyDecoder bodyDecoder = createMock(MessageBodyDecoder.class);
        expect(bodyDecoder.decode(msgType, buf, bodyLen)).andReturn(msgBody);

        Channel channel = createMockChannel(1);

        replay(bodyDecoder);
        replay(channel);

        NetConfig netConfig = new NetConfig();
        netConfig.setShareChannel(true);
        netConfig.setEnableRequestMode(true);

        NetQpMessageDecoder decoder = new NetQpMessageDecoder(netConfig, bodyDecoder);
        ShareChannelMessage msg = (ShareChannelMessage) decoder
                .decode(createMock(ChannelHandlerContext.class), channel, buf);

        assertEquals(msgType, msg.getType());
        assertSame(msgBody, msg.getBody());
        assertEquals(sid, msg.getSessionId());
        assertEquals(requestId, msg.getRequestId());
        assertEquals(requestMode, msg.getRequestMode());

        verify(bodyDecoder);
        verify(channel);
    }

    @Test
    public void testDecodeNoRequest() throws Exception {
        int msgType = 1;

        ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
        int requestMode = NetMessage.QP_NONE;
        buf.writeShort(NetMessageHelper.createRequestModeFlag(0, requestMode)); // flag
        buf.writeShort(msgType);
        int bodyLen = 0;
        buf.writeInt(bodyLen);

        CgMockLogin msgBody = new CgMockLogin("101");

        MessageBodyDecoder bodyDecoder = createMock(MessageBodyDecoder.class);
        expect(bodyDecoder.decode(msgType, buf, bodyLen)).andReturn(msgBody);

        Channel channel = createMockChannel(1);

        replay(bodyDecoder);
        replay(channel);

        NetConfig netConfig = new NetConfig();
        netConfig.setEnableRequestMode(true);

        NetQpMessageDecoder decoder = new NetQpMessageDecoder(netConfig, bodyDecoder);
        NetMessage msg = (NetMessage) decoder.decode(createMock(ChannelHandlerContext.class),
                channel, buf);

        assertEquals(msgType, msg.getType());
        assertSame(msgBody, msg.getBody());
        assertEquals(requestMode, msg.getRequestMode());

        verify(bodyDecoder);
        verify(channel);
    }

    /**
     * 测试没有收到足够数据的情况
     *
     * @throws Exception
     */
    @Test
    public void testDecodeMessgeNotEnoughData() throws Exception {
        int msgType = 1;

        ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
        int requestMode = NetMessage.QP_REQUEST;
        buf.writeShort(NetMessageHelper.createRequestModeFlag(0, requestMode)); // flag
        buf.writeShort(msgType);
        buf.writeInt(0); // bodyLen
        // 没有写requestId，后面读取时，应该数据不够

        MessageBodyDecoder bodyDecoder = createMock(MessageBodyDecoder.class);

        Channel channel = createMockChannel(1);

        replay(bodyDecoder);
        replay(channel);

        NetConfig netConfig = new NetConfig();
        netConfig.setEnableRequestMode(true);

        NetQpMessageDecoder decoder = new NetQpMessageDecoder(netConfig, bodyDecoder);
        int readerIdx = buf.readerIndex();
        Object decodeRst = decoder.decode(createMock(ChannelHandlerContext.class), channel, buf);

        assertNull(decodeRst); // 数据不够应该返回null
        assertEquals(readerIdx, buf.readerIndex()); // 数据不够应该恢复reader的索引位置

        verify(bodyDecoder);
        verify(channel);
    }

    private Channel createMockChannel(int sid) {
        NetSession session = new NetSession(sid);

        Channel channel = createMock(Channel.class);
        expect(channel.getAttachment()).andReturn(session).anyTimes();

        session.setChannel(channel);

        return channel;
    }

}
