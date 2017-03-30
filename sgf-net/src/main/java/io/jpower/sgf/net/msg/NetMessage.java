package io.jpower.sgf.net.msg;

/**
 * <ul>
 * <li>总共分为5部分</li>
 * <li>第1部分，标志位，占2字节
 * <ul>
 * <li>第15位，是否压缩</li>
 * <li>第7位和第6位，请求响应模式</li>
 * <li>其他暂时保留</li>
 * </ul>
 * </li>
 * <li>第2部分，sequence，占用2字节，无符号，范围0 - 65535，<strong>这部分为可选，现在只用于客户端</strong></li>
 * <li>第3部分，消息类型，占用2字节，无符号，范围0 - 65535</li>
 * <li>第4部分，消息体长度，占用4字节，范围0 -
 * 2147483647，负数部分暂时未使用。其实不应该有这么大的消息体，用3字节长度足够。但是3字节看着有些别扭，所以就4字节了。</li>
 * <li>第5部分，请求id，占用4字节，范围0 - 2147483647，负数部分暂时未使用，<strong>这部分为可选</strong></li>
 * <li>第6部分，消息体，具体格式可以自定义</li>
 * </ul>
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class NetMessage implements IMessage {

    public static final int FLAG_SIZE = 2;

    public static final int SEQUENCE_SIZE = 2;

    public static final int TYPE_SIZE = 2;

    public static final int BODY_LENGTH_SIZE = 4;

    // public static final int RESERVED_SIZE = 1;

    public static final int HEADER_SIZE = FLAG_SIZE + TYPE_SIZE
            + BODY_LENGTH_SIZE /* + RESERVED_SIZE */;

    public static final int FLAG_COMPRESSED_SHIFT = 15;

    public static final int REQUEST_ID_SIZE = 4;

    public static final int FLAG_REQUEST_MODE_SHIFT = 6;

    public static final int QP_NONE = 0;

    public static final int QP_REQUEST = 1;

    public static final int QP_RESPONSE = 2;

    private short flag;

    private int requestMode = QP_NONE;

    private int sequence;

    private int type = -1;

    private int requestId = -1;

    private Object body;

    private Object owner;

    public short getFlag() {
        return flag;
    }

    public void setFlag(short flag) {
        this.flag = flag;
    }

    public int getRequestMode() {
        return requestMode;
    }

    public void setRequestMode(int requestMode) {
        this.requestMode = requestMode;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public Object getOwner() {
        return owner;
    }

    public void setOwner(Object owner) {
        this.owner = owner;
    }

    @Override
    public String getName() {
        return body.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getType() + "-" + getName();
    }

}
