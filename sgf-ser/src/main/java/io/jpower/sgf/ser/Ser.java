package io.jpower.sgf.ser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import io.jpower.sgf.utils.JavaUtils;

/**
 * 提供序列化和反序列化的Facade
 * <p>
 * <ul>
 * <li>线程安全的</li>
 * </ul>
 *
 * @author zheng.sun
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
     * 字节流大小限制
     */
    private final int byteSizeLimit;

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
        this(4096, 2048);
    }

    public Ser(int byteSizeLimit, int containerSizeLimit) {
        this.byteSizeLimit = byteSizeLimit;
        this.containerSizeLimit = containerSizeLimit;
    }

    /**
     * 将一个对象序列化为二进制并以字节数组返回
     *
     * @param obj
     * @return
     */
    public byte[] serialize(Object obj) {
        return serialize(obj, false);
    }

    /**
     * 将一个对象序列化为二进制并以字节数组返回
     *
     * @param obj
     * @param compress 是否压缩
     * @return
     */
    public byte[] serialize(Object obj, boolean compress) {
        if (compress) {
            ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream(128);
            Deflater deflater = new Deflater();
            DeflaterOutputStream deflaterOutput = new DeflaterOutputStream(byteArrayOutput,
                    deflater);
            try {
                serialize0(obj, deflaterOutput);
                deflaterOutput.finish();
                return byteArrayOutput.toByteArray();
            } catch (IOException e) {
                throw JavaUtils.sneakyThrow(e);
            } finally {
                deflater.end();
            }
        } else {
            ByteArrayWriter byteArrayWriter = new ByteArrayWriter();
            SerContext ctx = new SerContext(byteArrayWriter);
            SER_WRITER.write(ctx, obj);
            return byteArrayWriter.toByteArray();
        }
    }

    /**
     * 将一个对象序列化为二进制并写入到流中
     *
     * @param obj
     * @param output
     */
    public void serialize(Object obj, OutputStream output) {
        serialize(obj, output, false);
    }

    /**
     * 将一个对象序列化为二进制并写入到流中
     *
     * @param obj
     * @param output
     * @param compress 是否压缩
     */
    public void serialize(Object obj, OutputStream output, boolean compress) {
        if (compress) {
            Deflater deflater = new Deflater();
            DeflaterOutputStream deflaterOutput = new DeflaterOutputStream(output, deflater);

            try {
                serialize0(obj, deflaterOutput);
                deflaterOutput.finish();
            } catch (IOException e) {
                throw JavaUtils.sneakyThrow(e);
            } finally {
                deflater.end();
            }
        } else {
            serialize0(obj, output);
        }
    }

    private void serialize0(Object obj, OutputStream output) {
        StreamWriter streamWriter = new StreamWriter(output);
        SerContext ctx = new SerContext(streamWriter);
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
        return deserialize(data, type, false);
    }

    /**
     * 根据指定字节数组进行反序列化
     *
     * @param data
     * @param type
     * @param compress 是否压缩
     * @return
     */
    public <T> T deserialize(byte[] data, Class<T> type, boolean compress) {
        if (compress) {
            ByteArrayInputStream byteArrayInput = new ByteArrayInputStream(data);
            Inflater inflater = new Inflater();
            InflaterInputStream deflaterInput = new InflaterInputStream(byteArrayInput, inflater);
            try {
                T rst = deserialize0(deflaterInput, type);
                return rst;
            } finally {
                inflater.end();
            }
        } else {
            ByteArrayReader byteArrayReader = new ByteArrayReader(data);
            DeserContext ctx = new DeserContext(byteArrayReader, byteSizeLimit, containerSizeLimit);
            return SER_READER.read(ctx, type);
        }
    }

    /**
     * 根据指定输入流中的数据进行反序列化
     *
     * @param input
     * @param type
     * @return
     */
    public <T> T deserialize(InputStream input, Class<T> type) {
        return deserialize(input, type, false);
    }

    /**
     * 根据指定输入流中的数据进行反序列化
     *
     * @param input
     * @param type
     * @param compress 是否压缩
     * @return
     */
    public <T> T deserialize(InputStream input, Class<T> type, boolean compress) {
        if (compress) {
            Inflater inflater = new Inflater();
            InflaterInputStream deflaterInput = new InflaterInputStream(input, inflater);
            try {
                T rst = deserialize0(deflaterInput, type);
                return rst;
            } finally {
                inflater.end();
            }
        } else {
            return deserialize0(input, type);
        }
    }

    private <T> T deserialize0(InputStream input, Class<T> type) {
        StreamReader streamReader = new StreamReader(input);
        DeserContext ctx = new DeserContext(streamReader, byteSizeLimit, containerSizeLimit);
        return SER_READER.read(ctx, type);
    }

}
