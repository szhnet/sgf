package io.jpower.sgf.net.codec.thrift;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TTupleProtocol;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Before;
import org.junit.Test;

import io.jpower.sgf.net.msg.CgMockLogin;
import io.jpower.sgf.net.msg.MessageConfigManager;
import io.jpower.sgf.net.msg.MessageConfig.MessageMeta;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class ThriftBodyDecoderTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testDecode() throws Exception {
        CgMockLogin msgBody = new CgMockLogin("101");

        // 用thrift来序列化成字节数组，并包装成ChannelBuffer
        TSerializer serializer = new TSerializer(new TTupleProtocol.Factory());
        byte[] thriftSerializerBytes = serializer.serialize(msgBody);
        ChannelBuffer buf = ChannelBuffers.wrappedBuffer(thriftSerializerBytes);

        int msgType = 1;

        @SuppressWarnings("unchecked")
        MessageConfigManager<MessageMeta> msgConfigMgr = createMock(MessageConfigManager.class);
        expect(msgConfigMgr.<CgMockLogin>getMessageBodyClass(msgType)).andReturn(CgMockLogin.class);

        replay(msgConfigMgr);

        ThriftBodyDecoder bodyDecoder = new ThriftBodyDecoder(new TTupleProtocol.Factory(),
                msgConfigMgr);
        Object decodeRst = bodyDecoder.decode(msgType, buf, thriftSerializerBytes.length);
        assertEquals(msgBody, decodeRst);

        verify(msgConfigMgr);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetProtocolFactoryDuplicate() {
        ThriftBodyDecoder bodyDecoder = new ThriftBodyDecoder();
        bodyDecoder.setProtocolFactory(new TTupleProtocol.Factory()); // 可以设置，不会报错

        try {
            bodyDecoder.setProtocolFactory(new TCompactProtocol.Factory());
            fail(); // 上一步应该会抛异常
        } catch (Exception e) {
            // success
        }

        // 再试一下通过构造设置ProtocolFactory的情况
        bodyDecoder = new ThriftBodyDecoder(new TTupleProtocol.Factory(),
                createMock(MessageConfigManager.class));

        try {
            bodyDecoder.setProtocolFactory(new TCompactProtocol.Factory());
            fail(); // 上一步应该会抛异常
        } catch (Exception e) {
            // success
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetProtocolFactoryNull() {
        ThriftBodyDecoder bodyDecoder = new ThriftBodyDecoder();

        try {
            bodyDecoder.setProtocolFactory(null);
            fail(); // 上一步应该会抛异常
        } catch (NullPointerException e) {
            // success
        }

        // 再试一下通过构造设置ProtocolFactory的情况
        try {
            new ThriftBodyDecoder(null, createMock(MessageConfigManager.class));
            fail(); // 上一步应该会抛异常
        } catch (NullPointerException e) {
            // success
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetMessageConfigManagerDuplicate() {
        ThriftBodyDecoder bodyDncoder = new ThriftBodyDecoder();
        bodyDncoder.setMessageConfigManager(createMock(MessageConfigManager.class)); // 可以设置，不会报错

        try {
            bodyDncoder.setMessageConfigManager(createMock(MessageConfigManager.class));
            fail(); // 上一步应该会抛异常
        } catch (Exception e) {
            // success
        }

        // 再试一下通过构造设置MessageConfigManager的情况
        bodyDncoder = new ThriftBodyDecoder(new TTupleProtocol.Factory(),
                createMock(MessageConfigManager.class));

        try {
            bodyDncoder.setMessageConfigManager(createMock(MessageConfigManager.class));
            fail(); // 上一步应该会抛异常
        } catch (Exception e) {
            // success
        }
    }

    @Test
    public void testSetMessageConfigManagerNull() {
        ThriftBodyDecoder bodyDecoder = new ThriftBodyDecoder();

        try {
            bodyDecoder.setMessageConfigManager(null);
            fail(); // 上一步应该会抛异常
        } catch (NullPointerException e) {
            // success
        }

        // 再试一下通过构造设置MessageConfigManager的情况
        try {
            new ThriftBodyDecoder(new TTupleProtocol.Factory(), null);
            fail(); // 上一步应该会抛异常
        } catch (NullPointerException e) {
            // success
        }
    }

}
