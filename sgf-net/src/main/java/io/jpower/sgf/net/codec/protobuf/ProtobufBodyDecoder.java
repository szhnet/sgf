package io.jpower.sgf.net.codec.protobuf;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.jpower.sgf.net.codec.MessageBodyDecoder;
import io.jpower.sgf.net.common.NettyChannelBufferInputStream;
import io.jpower.sgf.net.msg.MessageConfig;
import io.jpower.sgf.net.msg.MessageConfigManager;
import io.jpower.sgf.thread.Sharable;
import org.jboss.netty.buffer.ChannelBuffer;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Parser;
import io.jpower.sgf.net.msg.MessageConfig.MessageMeta;

/**
 * <a href=https://developers.google.com/protocol-buffers/>Protocol
 * Buffers</a>消息体解码器
 *
 * @author zheng.sun
 */
@Sharable
public class ProtobufBodyDecoder implements MessageBodyDecoder {

    private final ThreadLocal<CodedInputStream> codedInputStreams = new ThreadLocal<CodedInputStream>();

    private final ThreadLocal<NettyChannelBufferInputStream> nettyInputStreams = new ThreadLocal<NettyChannelBufferInputStream>();

    /**
     * 用来缓存Protobuf消息的parser
     */
    private final ConcurrentMap<Class<?>, Parser<?>> parserCache = new ConcurrentHashMap<Class<?>, Parser<?>>();

    private MessageConfigManager<MessageMeta> messageConfigManager;

    public ProtobufBodyDecoder() {

    }

    public ProtobufBodyDecoder(MessageConfigManager<MessageConfig.MessageMeta> messageConfigManager) {
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

    @Override
    public Object decode(int msgType, ChannelBuffer buffer, int bodyLength) throws Exception {
        Class<?> msgBodyClass = messageConfigManager.getMessageBodyClass(msgType);
        if (msgBodyClass == null) {
            throw new IllegalArgumentException("Not found message Type mapping: " + msgType);
        }

        Parser<?> parser = parserCache.get(msgBodyClass);
        if (parser == null) {
            Field parserField = msgBodyClass.getDeclaredField("PARSER");
            parser = (Parser<?>) parserField.get(null);
            Parser<?> existsParser = parserCache.putIfAbsent(msgBodyClass, parser);
            if (existsParser != null) {
                parser = existsParser;
            }
        }

        CodedInputStream cis = getCodedInputStream();
        NettyChannelBufferInputStream nis = nettyInputStreams.get();
        nis.buffer(buffer, bodyLength); // 设置buffer
        try {
            Object msgBody = parser.parseFrom(cis);
            return msgBody;
        } finally {
            nis.clearBuffer(); // 清除之前设置的buffer
        }
    }

    private CodedInputStream getCodedInputStream() {
        CodedInputStream cis = codedInputStreams.get();
        if (cis == null) {
            NettyChannelBufferInputStream nis = new NettyChannelBufferInputStream();
            cis = CodedInputStream.newInstance(nis);
            nettyInputStreams.set(nis); // CodedInputStream设置的InputStream并不能通过方法访问，但是使用时需要对InputStream进行特殊操作，所以这里把InputStream也单独存一下。
            codedInputStreams.set(cis);
        }
        return cis;
    }

}
