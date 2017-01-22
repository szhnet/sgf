package io.jpower.sgf.net.msg;

/**
 * 消息处理器
 *
 * @author zheng.sun
 */
public interface MessageHandler<T, S> {

    /**
     * 处理消息
     *
     * @param message 待处理的消息
     * @param session session
     */
    void handle(T message, S session);

}
