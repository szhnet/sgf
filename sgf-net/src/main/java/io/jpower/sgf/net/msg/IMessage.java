package io.jpower.sgf.net.msg;

/**
 * 消息接口定义
 *
 * @author zheng.sun
 */
public interface IMessage {

    /**
     * 内部消息号0
     */
    public static final int INTERNEL_MESSAGE_TYPE = 0;

    /**
     * 消息的类型
     * <p>
     * <li>范围 0 - 65535</li>
     * <li>0 一般作为内部系统保留的类型</li>
     *
     * @return
     */
    int getType();

    /**
     * 消息名称
     *
     * @return
     */
    String getName();

}
