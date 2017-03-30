package io.jpower.sgf.net.codec.thrift;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import io.jpower.sgf.net.msg.GcMockLoginInfo;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TTupleProtocol;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class ThriftBodyEncoderTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSetProtocolFactoryDuplicate() {
        ThriftBodyEncoder bodyEncoder = new ThriftBodyEncoder();
        bodyEncoder.setProtocolFactory(new TTupleProtocol.Factory()); // 可以设置，不会报错

        try {
            bodyEncoder.setProtocolFactory(new TBinaryProtocol.Factory());
            fail(); // 上一步应该会抛异常
        } catch (Exception e) {
            // success
        }

        // 再试一下通过构造设置ProtocolFactory的情况
        bodyEncoder = new ThriftBodyEncoder(new TTupleProtocol.Factory());

        try {
            bodyEncoder.setProtocolFactory(new TBinaryProtocol.Factory());
            fail(); // 上一步应该会抛异常
        } catch (Exception e) {
            // success
        }
    }

    @Test
    public void testSetProtocolFactoryNull() {
        ThriftBodyEncoder bodyEncoder = new ThriftBodyEncoder();

        try {
            bodyEncoder.setProtocolFactory(null);
            fail(); // 上一步应该会抛异常
        } catch (NullPointerException e) {
            // success
        }

        // 再试一下通过构造设置ProtocolFactory的情况
        try {
            new ThriftBodyEncoder(null);
            fail(); // 上一步应该会抛异常
        } catch (NullPointerException e) {
            // success
        }
    }

    @Test
    public void testCanEncode() {
        ThriftBodyEncoder bodyEncoder = new ThriftBodyEncoder();
        assertTrue(bodyEncoder.canEncode(new GcMockLoginInfo()));
        assertFalse(bodyEncoder.canEncode(new ArrayList<Integer>()));
    }

    @Test
    public void testEncode() throws Exception {
        GcMockLoginInfo msgBody = new GcMockLoginInfo(System.currentTimeMillis(), false);

        // 用thrift来序列化成字节数组
        TSerializer serializer = new TSerializer(new TTupleProtocol.Factory());
        byte[] thriftSerializerBytes = serializer.serialize(msgBody);

        // 用encode写入ChannelBuffer
        ThriftBodyEncoder bodyEncoder = new ThriftBodyEncoder(new TTupleProtocol.Factory());
        ChannelBuffer buf = ChannelBuffers.dynamicBuffer(128);
        bodyEncoder.encode(msgBody, buf);
        byte[] encodeBytes = new byte[buf.readableBytes()];
        buf.getBytes(0, encodeBytes);

        assertArrayEquals(thriftSerializerBytes, encodeBytes);
    }

}
