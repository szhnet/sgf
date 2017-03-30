package io.jpower.sgf.net.msg;

/**
 * 消息处理器
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public interface MessageHandlerFactory {

    /**
     * 根据消息类型获得一个消息处理器
     *
     * @param msgType 消息类型
     * @return 返回相应的消息处理器
     */
    <H extends MessageHandler<?, ?>> H getMessageHandler(int msgType);

}
