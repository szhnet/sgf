package io.jpower.sgf.net;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.IOException;

import io.jpower.sgf.net.codec.MessageBodyDecoder;
import io.jpower.sgf.net.msg.NetMessage;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.SucceededChannelFuture;
import org.junit.Before;
import org.junit.Test;

import io.jpower.sgf.net.msg.CgMockLogin;
import io.jpower.sgf.net.msg.ShareChannelMessage;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class NetMessageDecoderTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testDecode() throws Exception {
        int msgType = 1;

        ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
        buf.writeShort(0); // flag
        buf.writeShort(msgType);
        int bodyLen = 0;
        buf.writeInt(bodyLen);

        CgMockLogin msgBody = new CgMockLogin("101");

        MessageBodyDecoder bodyDecoder = createMock(MessageBodyDecoder.class);
        expect(bodyDecoder.decode(msgType, buf, bodyLen)).andReturn(msgBody);

        Channel channel = createMockChannel(1);

        replay(bodyDecoder);
        replay(channel);

        NetMessageDecoder decoder = new NetMessageDecoder(new NetConfig(), bodyDecoder);
        NetMessage msg = (NetMessage) decoder.decode(createMock(ChannelHandlerContext.class),
                channel, buf);

        assertEquals(msgType, msg.getType());
        assertSame(msgBody, msg.getBody());

        verify(bodyDecoder);
        verify(channel);
    }

    @Test
    public void testShareSessionDecode() throws Exception {
        int msgType = 1;

        ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
        buf.writeShort(0); // flag
        buf.writeShort(msgType);
        int bodyLen = 0;
        buf.writeInt(bodyLen);
        int sid = 10;
        buf.writeInt(sid); // sid

        CgMockLogin msgBody = new CgMockLogin("101");

        MessageBodyDecoder bodyDecoder = createMock(MessageBodyDecoder.class);
        expect(bodyDecoder.decode(msgType, buf, bodyLen)).andReturn(msgBody);

        Channel channel = createMockChannel(1);

        replay(bodyDecoder);
        replay(channel);

        NetConfig netConfig = new NetConfig();
        netConfig.setShareChannel(true);

        NetMessageDecoder decoder = new NetMessageDecoder(netConfig, bodyDecoder);
        ShareChannelMessage msg = (ShareChannelMessage) decoder.decode(
                createMock(ChannelHandlerContext.class), channel, buf);

        assertEquals(msgType, msg.getType());
        assertSame(msgBody, msg.getBody());
        assertEquals(sid, msg.getSessionId());

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
        buf.writeShort(0); // flag
        buf.writeShort(msgType);
        buf.writeInt(10); // bodyLen

        MessageBodyDecoder bodyDecoder = createMock(MessageBodyDecoder.class);

        Channel channel = createMockChannel(1);

        replay(bodyDecoder);
        replay(channel);

        NetMessageDecoder decoder = new NetMessageDecoder(new NetConfig(), bodyDecoder);
        int readerIdx = buf.readerIndex();
        Object decodeRst = decoder.decode(createMock(ChannelHandlerContext.class), channel, buf);

        assertNull(decodeRst); // 数据不够应该返回null
        assertEquals(readerIdx, buf.readerIndex()); // 数据不够应该恢复reader的索引位置

        verify(bodyDecoder);
        verify(channel);
    }

    @Test
    public void testDecodeFailed() throws Exception {
        int msgType = 1;

        ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
        buf.writeShort(0); // flag
        buf.writeShort(msgType);
        int bodyLen = 0;
        buf.writeInt(bodyLen);

        MessageBodyDecoder bodyDecoder = createMock(MessageBodyDecoder.class);
        expect(bodyDecoder.decode(msgType, buf, bodyLen)).andThrow(
                new IOException("testDecodeFailed")); // 让bodyDecoder抛出异常

        Channel channel = createMockChannel(1);
        expect(channel.close()).andReturn(new SucceededChannelFuture(channel)); // 解码失败会关闭channel

        replay(bodyDecoder);
        replay(channel);

        NetMessageDecoder decoder = new NetMessageDecoder(new NetConfig(), bodyDecoder);

        int readerIdx = buf.readerIndex();
        Object decodeRst = decoder.decode(createMock(ChannelHandlerContext.class), channel, buf);

        assertNull(decodeRst); // 解码失败应该返回null
        assertEquals(readerIdx, buf.readerIndex()); // 应该恢复reader的索引位置

        verify(bodyDecoder);
        verify(channel);
    }

    @Test
    public void testDecodeMessgeBodyLengthTooLong() throws Exception {
        ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
        buf.writeShort(0); // flag
        buf.writeShort(1);
        buf.writeInt(128 * 1024); // bodyLen

        MessageBodyDecoder bodyDecoder = createMock(MessageBodyDecoder.class);

        Channel channel = createMockChannel(1);
        expect(channel.close()).andReturn(new SucceededChannelFuture(channel)); // 检查到异常会关闭channel

        replay(bodyDecoder);
        replay(channel);

        NetMessageDecoder decoder = new NetMessageDecoder(new NetConfig(), bodyDecoder, 64 * 1024,
                128 * 1024);

        int readerIdx = buf.readerIndex();
        Object decodeRst = decoder.decode(createMock(ChannelHandlerContext.class), channel, buf);

        assertNull(decodeRst); // 解码失败应该返回null
        assertEquals(readerIdx, buf.readerIndex()); // 应该恢复reader的索引位置

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
