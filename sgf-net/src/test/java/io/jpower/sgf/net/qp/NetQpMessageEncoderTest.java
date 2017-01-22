package io.jpower.sgf.net.qp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import io.jpower.sgf.net.NetConfig;
import io.jpower.sgf.net.NetMessageEncoderTest;
import io.jpower.sgf.net.NetMessageHelper;
import io.jpower.sgf.net.ShareChannelSession;
import io.jpower.sgf.net.msg.GcMockLoginInfo;
import io.jpower.sgf.net.msg.MessageConfig;
import io.jpower.sgf.net.msg.MessageConfigManager;
import io.jpower.sgf.net.msg.NetMessage;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.junit.Test;

import io.jpower.sgf.net.msg.MessageConfig.MessageMeta;
import io.jpower.sgf.net.NetSession;
import io.jpower.sgf.net.ShareChannelMessageWrapper;

/**
 * @author zheng.sun
 */
public class NetQpMessageEncoderTest {

    @Test
    public void testEncodeRequest() throws Exception {
        int messageType = 2;
        int flag = NetMessageHelper.createRequestModeFlag(0, NetMessage.QP_REQUEST);
        int requestId = 6;

        @SuppressWarnings("unchecked")
        MessageConfigManager<MessageMeta> msgMgr = createMock(MessageConfigManager.class);
        expect(msgMgr.getMessageType(GcMockLoginInfo.class)).andReturn(messageType);

        Channel channel = createMockChannel(1);

        byte[] encodeBytes = {8, 1, 8};

        GcMockLoginInfo msgBody = new GcMockLoginInfo(System.currentTimeMillis(), false);
        RequestContext<GcMockLoginInfo, Object> q = new RequestContext<GcMockLoginInfo, Object>(
                requestId, new RequestCallback<GcMockLoginInfo, Object>() {

            @Override
            public void callback(NetSession session,
                                 RequestContext<GcMockLoginInfo, Object> ctx) {
                // nothing
            }
        });
        q.setRequestMessage(msgBody);

        replay(channel);
        replay(msgMgr);

        NetConfig netConfig = new NetConfig();
        netConfig.setEnableRequestMode(true);

        NetQpMessageEncoder encoder = new NetQpMessageEncoder(netConfig,
                new NetMessageEncoderTest.MockBodyEncoder(encodeBytes), msgMgr);

        ChannelBuffer buf = (ChannelBuffer) encoder.encode(createMock(ChannelHandlerContext.class),
                channel, q);
        // 检查消息头
        short actFlag = buf.readShort();
        short actMsgType = buf.readShort();
        int actBodyLen = buf.readInt();
        int actReqId = buf.readInt();

        assertEquals(flag, actFlag);
        assertEquals(messageType, actMsgType);
        assertEquals(encodeBytes.length, actBodyLen);
        assertEquals(requestId, actReqId);

        // 检查消息体
        byte[] actualEncodeBytes = new byte[encodeBytes.length];
        buf.readBytes(actualEncodeBytes);
        assertArrayEquals(encodeBytes, actualEncodeBytes);

        assertFalse(buf.readable()); // 应该读到尾了

        verify(channel);
        verify(msgMgr);
    }

    @Test
    public void testShareSessionEncodeRequest() throws Exception {
        int messageType = 2;
        int flag = NetMessageHelper.createRequestModeFlag(0, NetMessage.QP_REQUEST);
        int requestId = 6;

        @SuppressWarnings("unchecked")
        MessageConfigManager<MessageConfig.MessageMeta> msgMgr = createMock(MessageConfigManager.class);
        expect(msgMgr.getMessageType(GcMockLoginInfo.class)).andReturn(messageType);

        Channel channel = createMockChannel(1);

        int sid = 10;
        ShareChannelSession scs = new ShareChannelSession(sid);

        byte[] encodeBytes = {8, 1, 8};

        GcMockLoginInfo msgBody = new GcMockLoginInfo(System.currentTimeMillis(), false);
        RequestContext<GcMockLoginInfo, Object> q = new RequestContext<GcMockLoginInfo, Object>(
                requestId, new RequestCallback<GcMockLoginInfo, Object>() {

            @Override
            public void callback(NetSession session,
                                 RequestContext<GcMockLoginInfo, Object> ctx) {
                // nothing
            }
        });
        q.setRequestMessage(msgBody);
        ShareChannelMessageWrapper smsg = new ShareChannelMessageWrapper(scs, q);

        replay(channel);
        replay(msgMgr);

        NetConfig netConfig = new NetConfig();
        netConfig.setShareChannel(true);
        netConfig.setEnableRequestMode(true);

        NetQpMessageEncoder encoder = new NetQpMessageEncoder(netConfig,
                new NetMessageEncoderTest.MockBodyEncoder(encodeBytes), msgMgr);

        ChannelBuffer buf = (ChannelBuffer) encoder.encode(createMock(ChannelHandlerContext.class),
                channel, smsg);
        // 检查消息头
        short actFlag = buf.readShort();
        short actMsgType = buf.readShort();
        int actBodyLen = buf.readInt();
        int actSid = buf.readInt();
        int actReqId = buf.readInt();

        assertEquals(flag, actFlag);
        assertEquals(messageType, actMsgType);
        assertEquals(encodeBytes.length, actBodyLen);
        assertEquals(sid, actSid);
        assertEquals(requestId, actReqId);

        // 检查消息体
        byte[] actualEncodeBytes = new byte[encodeBytes.length];
        buf.readBytes(actualEncodeBytes);
        assertArrayEquals(encodeBytes, actualEncodeBytes);

        assertFalse(buf.readable()); // 应该读到尾了

        verify(channel);
        verify(msgMgr);
    }

    @Test
    public void testEncodeResponse() throws Exception {
        int messageType = 2;
        int flag = NetMessageHelper.createRequestModeFlag(0, NetMessage.QP_RESPONSE);
        int requestId = 6;

        @SuppressWarnings("unchecked")
        MessageConfigManager<MessageConfig.MessageMeta> msgMgr = createMock(MessageConfigManager.class);
        expect(msgMgr.getMessageType(GcMockLoginInfo.class)).andReturn(2);

        Channel channel = createMockChannel(1);

        byte[] encodeBytes = {8, 1, 8};

        GcMockLoginInfo msgBody = new GcMockLoginInfo(System.currentTimeMillis(), false);
        Response<GcMockLoginInfo> p = new Response<GcMockLoginInfo>(requestId, msgBody);

        replay(channel);
        replay(msgMgr);

        NetConfig netConfig = new NetConfig();
        netConfig.setEnableRequestMode(true);

        NetQpMessageEncoder encoder = new NetQpMessageEncoder(netConfig,
                new NetMessageEncoderTest.MockBodyEncoder(encodeBytes), msgMgr);

        ChannelBuffer buf = (ChannelBuffer) encoder.encode(createMock(ChannelHandlerContext.class),
                channel, p);
        // 检查消息头
        short actFlag = buf.readShort();
        short actMsgType = buf.readShort();
        int actBodyLen = buf.readInt();
        int actReqId = buf.readInt();

        assertEquals(flag, actFlag);
        assertEquals(messageType, actMsgType);
        assertEquals(encodeBytes.length, actBodyLen);
        assertEquals(requestId, actReqId);

        // 检查消息体
        byte[] actualEncodeBytes = new byte[encodeBytes.length];
        buf.readBytes(actualEncodeBytes);
        assertArrayEquals(encodeBytes, actualEncodeBytes);

        assertFalse(buf.readable()); // 应该读到尾了

        verify(channel);
        verify(msgMgr);
    }

    @Test
    public void testShareSessionEncodeResponse() throws Exception {
        int messageType = 2;
        int flag = NetMessageHelper.createRequestModeFlag(0, NetMessage.QP_RESPONSE);
        int requestId = 6;

        @SuppressWarnings("unchecked")
        MessageConfigManager<MessageConfig.MessageMeta> msgMgr = createMock(MessageConfigManager.class);
        expect(msgMgr.getMessageType(GcMockLoginInfo.class)).andReturn(messageType);

        Channel channel = createMockChannel(1);

        int sid = 10;
        ShareChannelSession scs = new ShareChannelSession(sid);

        byte[] encodeBytes = {8, 1, 8};

        GcMockLoginInfo msgBody = new GcMockLoginInfo(System.currentTimeMillis(), false);
        Response<GcMockLoginInfo> p = new Response<GcMockLoginInfo>(requestId, msgBody);
        ShareChannelMessageWrapper smsg = new ShareChannelMessageWrapper(scs, p);

        replay(channel);
        replay(msgMgr);

        NetConfig netConfig = new NetConfig();
        netConfig.setEnableRequestMode(true);
        netConfig.setShareChannel(true);

        NetQpMessageEncoder encoder = new NetQpMessageEncoder(netConfig,
                new NetMessageEncoderTest.MockBodyEncoder(encodeBytes), msgMgr);

        ChannelBuffer buf = (ChannelBuffer) encoder.encode(createMock(ChannelHandlerContext.class),
                channel, smsg);
        // 检查消息头
        short actFlag = buf.readShort();
        short actMsgType = buf.readShort();
        int actBodyLen = buf.readInt();
        int actSid = buf.readInt();
        int actReqId = buf.readInt();

        assertEquals(flag, actFlag);
        assertEquals(messageType, actMsgType);
        assertEquals(encodeBytes.length, actBodyLen);
        assertEquals(sid, actSid);
        assertEquals(requestId, actReqId);

        // 检查消息体
        byte[] actualEncodeBytes = new byte[encodeBytes.length];
        buf.readBytes(actualEncodeBytes);
        assertArrayEquals(encodeBytes, actualEncodeBytes);

        assertFalse(buf.readable()); // 应该读到尾了

        verify(channel);
        verify(msgMgr);
    }

    @Test
    public void testEncode() throws Exception {
        int messageType = 2;
        int flag = NetMessageHelper.createRequestModeFlag(0, NetMessage.QP_NONE);

        @SuppressWarnings("unchecked")
        MessageConfigManager<MessageConfig.MessageMeta> msgMgr = createMock(MessageConfigManager.class);
        expect(msgMgr.getMessageType(GcMockLoginInfo.class)).andReturn(2);

        Channel channel = createMockChannel(1);

        byte[] encodeBytes = {8, 1, 8};

        GcMockLoginInfo msgBody = new GcMockLoginInfo(System.currentTimeMillis(), false);

        replay(channel);
        replay(msgMgr);

        NetConfig netConfig = new NetConfig();
        netConfig.setEnableRequestMode(true);

        NetQpMessageEncoder encoder = new NetQpMessageEncoder(netConfig,
                new NetMessageEncoderTest.MockBodyEncoder(encodeBytes), msgMgr);

        ChannelBuffer buf = (ChannelBuffer) encoder.encode(createMock(ChannelHandlerContext.class),
                channel, msgBody);
        // 检查消息头
        short actFlag = buf.readShort();
        short actMsgType = buf.readShort();
        int actBodyLen = buf.readInt();

        assertEquals(flag, actFlag);
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

    private Channel createMockChannel(int sid) {
        NetSession session = new NetSession(sid);

        Channel channel = createMock(Channel.class);
        expect(channel.getAttachment()).andReturn(session).anyTimes();

        session.setChannel(channel);

        return channel;
    }

}
