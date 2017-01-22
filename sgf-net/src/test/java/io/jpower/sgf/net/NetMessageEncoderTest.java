package io.jpower.sgf.net;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;

import io.jpower.sgf.net.msg.MessageConfig;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.Test;

import io.jpower.sgf.net.codec.MessageBodyEncoder;
import io.jpower.sgf.net.msg.GcMockLoginInfo;
import io.jpower.sgf.net.msg.MessageConfigManager;

/**
 * @author zheng.sun
 */
public class NetMessageEncoderTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testEncode() throws Exception {
        @SuppressWarnings("unchecked")
        MessageConfigManager<MessageConfig.MessageMeta> msgMgr = createMock(MessageConfigManager.class);
        int messageType = 2;
        expect(msgMgr.getMessageType(GcMockLoginInfo.class)).andReturn(messageType);

        Channel channel = createMockChannel(1);

        byte[] encodeBytes = {8, 1, 8};
        NetMessageEncoder encoder = new NetMessageEncoder(new NetConfig(), msgMgr,
                new MockBodyEncoder(encodeBytes));

        GcMockLoginInfo msgBody = new GcMockLoginInfo(System.currentTimeMillis(), false);

        replay(channel);
        replay(msgMgr);

        ChannelBuffer buf = (ChannelBuffer) encoder.encode(createMock(ChannelHandlerContext.class),
                channel, msgBody);
        // 检查消息头
        short actFlag = buf.readShort();
        short actMsgType = buf.readShort();
        int actBodyLen = buf.readInt();

        assertEquals(0, actFlag);
        assertEquals(messageType, actMsgType);
        assertEquals(encodeBytes.length, actBodyLen);

        // 检查消息体
        byte[] actualEncodeBytes = new byte[encodeBytes.length];
        buf.readBytes(actualEncodeBytes);
        assertArrayEquals(encodeBytes, actualEncodeBytes);

        assertFalse(buf.readable()); // 应该读到尾了

        verify(channel);
        verify(msgMgr);
    }

    @Test
    public void testShareSessionEncode() throws Exception {
        @SuppressWarnings("unchecked")
        MessageConfigManager<MessageConfig.MessageMeta> msgMgr = createMock(MessageConfigManager.class);
        int messageType = 2;
        expect(msgMgr.getMessageType(GcMockLoginInfo.class)).andReturn(messageType);

        Channel channel = createMockChannel(1);

        int sid = 10;
        ShareChannelSession scs = new ShareChannelSession(sid);

        byte[] encodeBytes = {8, 1, 8};

        GcMockLoginInfo msgBody = new GcMockLoginInfo(System.currentTimeMillis(), false);
        ShareChannelMessageWrapper smsg = new ShareChannelMessageWrapper(scs, msgBody);

        replay(channel);
        replay(msgMgr);

        NetConfig netConfig = new NetConfig();
        netConfig.setShareChannel(true);

        NetMessageEncoder encoder = new NetMessageEncoder(netConfig, msgMgr,
                new MockBodyEncoder(encodeBytes));
        ChannelBuffer buf = (ChannelBuffer) encoder.encode(createMock(ChannelHandlerContext.class),
                channel, smsg);
        // 检查消息头
        short actFlag = buf.readShort();
        short actMsgType = buf.readShort();
        int actBodyLen = buf.readInt();
        int actSid = buf.readInt();

        assertEquals(0, actFlag);
        assertEquals(messageType, actMsgType);
        assertEquals(encodeBytes.length, actBodyLen);
        assertEquals(sid, actSid);

        // 检查消息体
        byte[] actualEncodeBytes = new byte[encodeBytes.length];
        buf.readBytes(actualEncodeBytes);
        assertArrayEquals(encodeBytes, actualEncodeBytes);

        assertFalse(buf.readable()); // 应该读到尾了

        verify(channel);
        verify(msgMgr);
    }

    /**
     * 测试不支持的消息体
     *
     * @throws Exception
     */
    @Test
    public void testEncodeNotSupported() throws Exception {
        @SuppressWarnings("unchecked")
        MessageConfigManager<MessageConfig.MessageMeta> msgMgr = createMock(MessageConfigManager.class);

        Channel channel = createMockChannel(1);

        byte[] encodeBytes = {};
        NetMessageEncoder encoder = new NetMessageEncoder(new NetConfig(), msgMgr,
                new MockBodyEncoder(encodeBytes, false));

        replay(channel);
        replay(msgMgr);

        Object msgBody = new ArrayList<String>();
        Object encodeRst = encoder.encode(createMock(ChannelHandlerContext.class), channel,
                msgBody);
        assertSame(msgBody, encodeRst); // 不支持的类型将原样返回

        verify(channel);
        verify(msgMgr);
    }

    @Test
    public void testEncodeFailed() throws Exception {
        @SuppressWarnings("unchecked")
        MessageConfigManager<MessageConfig.MessageMeta> msgMgr = createMock(MessageConfigManager.class);
        expect(msgMgr.getMessageType(GcMockLoginInfo.class)).andReturn(-1); // 返回-1表示找不到消息类型

        Channel channel = createMockChannel(1);

        byte[] encodeBytes = {};
        NetMessageEncoder encoder = new NetMessageEncoder(new NetConfig(), msgMgr,
                new MockBodyEncoder(encodeBytes));

        GcMockLoginInfo msgBody = new GcMockLoginInfo(System.currentTimeMillis(), false);

        replay(channel);
        replay(msgMgr);

        Object encodeRst = encoder.encode(createMock(ChannelHandlerContext.class), channel,
                msgBody);
        assertNull(encodeRst); // 失败应该返回null

        verify(msgMgr);
        verify(channel);
    }

    private Channel createMockChannel(int sid) {
        NetSession session = new NetSession(sid);

        Channel channel = createMock(Channel.class);
        expect(channel.getAttachment()).andReturn(session).anyTimes();

        session.setChannel(channel);

        return channel;
    }

    /**
     * 消息体编码器的mock，直接写入预先设置好的字节数组
     */
    public static class MockBodyEncoder implements MessageBodyEncoder {

        private byte[] encodeBytes;

        private boolean canEncode;

        public MockBodyEncoder(byte[] encodeBytes) {
            this(encodeBytes, true);
        }

        public MockBodyEncoder(byte[] encodeBytes, boolean canEncode) {
            this.encodeBytes = encodeBytes;
            this.canEncode = canEncode;
        }

        @Override
        public boolean canEncode(Object msg) {
            return canEncode;
        }

        @Override
        public void encode(Object body, ChannelBuffer channelBuffer) throws Exception {
            channelBuffer.writeBytes(encodeBytes); // 直接写入指定的字节
        }

    }

}
