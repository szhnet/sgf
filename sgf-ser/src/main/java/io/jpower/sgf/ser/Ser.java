package io.jpower.sgf.ser;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 提供序列化和反序列化的Facade
 * <p>
 * <ul>
 * <li>线程安全的</li>
 * </ul>
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class Ser {

    private static final Ser INS = new Ser();

    /**
     * 表示没有数量大小限制
     */
    public static final int NO_SIZE_LIMIT = -1;

    private static final SerWriter SER_WRITER = new SerWriter();

    private static final SerReader SER_READER = new SerReader();

    /**
     * 最终序列化后的字节大小限制
     */
    private final int totalByteSizeLimit;

    /**
     * 容器大小限制
     */
    private final int containerSizeLimit;

    /**
     * 返回默认对象实例。一般用这个就行了。如果有特殊需求也可以自己创建对象。
     *
     * @return
     */
    public static Ser ins() {
        return INS;
    }

    public Ser() {
        this(NO_SIZE_LIMIT, NO_SIZE_LIMIT);
    }

    public Ser(int totalByteSizeLimit, int containerSizeLimit) {
        this.totalByteSizeLimit = totalByteSizeLimit;
        this.containerSizeLimit = containerSizeLimit;
    }

    /**
     * 将一个对象序列化为二进制并以字节数组返回
     *
     * @param obj
     * @return
     */
    public byte[] serialize(Object obj) {
        ByteArrayWriter byteArrayWriter = new ByteArrayWriter();
        SerContext ctx = new SerContext(byteArrayWriter, containerSizeLimit);
        SER_WRITER.write(ctx, obj);
        byte[] bytes = byteArrayWriter.toByteArray();

        if (totalByteSizeLimit != Ser.NO_SIZE_LIMIT && bytes.length > totalByteSizeLimit) {
            throw new SerializationException("Total byte size exceeded max allowed. size=" + bytes.length
                    + ", limit=" + totalByteSizeLimit + ", Object=" + obj);
        }
        return bytes;
    }

    /**
     * 将一个对象序列化为二进制并写入到流中
     *
     * @param obj
     * @param output
     */
    public void serialize(Object obj, OutputStream output) {
        StreamWriter streamWriter = new StreamWriter(output);
        SerContext ctx = new SerContext(streamWriter, containerSizeLimit);
        SER_WRITER.write(ctx, obj);
        streamWriter.flush();
    }

    /**
     * 根据指定字节数组进行反序列化
     *
     * @param data
     * @param type
     * @return
     */
    public <T> T deserialize(byte[] data, Class<T> type) {
        ByteArrayReader byteArrayReader = new ByteArrayReader(data);
        DeserContext ctx = new DeserContext(byteArrayReader);
        return SER_READER.read(ctx, type);
    }

    /**
     * 根据指定输入流中的数据进行反序列化
     *
     * @param input
     * @param type
     * @return
     */
    public <T> T deserialize(InputStream input, Class<T> type) {
        StreamReader streamReader = new StreamReader(input);
        DeserContext ctx = new DeserContext(streamReader);
        return SER_READER.read(ctx, type);
    }

}
