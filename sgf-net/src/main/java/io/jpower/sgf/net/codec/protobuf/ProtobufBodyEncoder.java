package io.jpower.sgf.net.codec.protobuf;

import io.jpower.sgf.net.common.NettyChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffer;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.GeneratedMessage;
import io.jpower.sgf.net.codec.MessageBodyEncoder;
import io.jpower.sgf.thread.Sharable;

/**
 * <a href=https://developers.google.com/protocol-buffers/>Protocol
 * Buffers</a>消息体编码器
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
@Sharable
public class ProtobufBodyEncoder implements MessageBodyEncoder {

    private final ThreadLocal<CodedOutputStream> codedOutputStreams = new ThreadLocal<CodedOutputStream>();

    private final ThreadLocal<NettyChannelBufferOutputStream> nettyOutputStreams = new ThreadLocal<NettyChannelBufferOutputStream>();

    public ProtobufBodyEncoder() {

    }

    @Override
    public boolean canEncode(Object msg) {
        return msg instanceof GeneratedMessage;
    }

    @Override
    public void encode(Object body, ChannelBuffer channelBuffer) throws Exception {
        GeneratedMessage pbody = (GeneratedMessage) body;

        CodedOutputStream cos = getCodedOutputStream();
        NettyChannelBufferOutputStream nos = nettyOutputStreams.get();

        nos.buffer(channelBuffer); // 设置buffer
        try {
            pbody.writeTo(cos);
            cos.flush(); // flush
        } finally {
            nos.clearBuffer(); // 清除之前设置的buffer
        }
    }

    private CodedOutputStream getCodedOutputStream() {
        CodedOutputStream cos = codedOutputStreams.get();
        if (cos == null) {
            NettyChannelBufferOutputStream nos = new NettyChannelBufferOutputStream();
            cos = CodedOutputStream.newInstance(nos);
            nettyOutputStreams.set(nos); // CodedOutputStream设置的OutputStream并不能通过方法访问，但是使用时需要对OutputStream进行特殊操作，所以这里把OutputStream也单独存一下。
            codedOutputStreams.set(cos);
        }
        return cos;
    }

}
