package io.jpower.sgf.net.msg;

import io.jpower.sgf.net.msg.MessageConfig.MessageMeta;

/**
 * 内部用一个数组来保存handler
 *
 * @author zheng.sun
 */
public class ArraySpringMessageHandlerFactory extends AbstractSpringMessageHandlerFactory {

    private int size = 0;

    private MessageHandler<?, ?>[] handlers = null;

    public ArraySpringMessageHandlerFactory() {

    }

    public ArraySpringMessageHandlerFactory(int size) {
        setSize(size);
    }

    @Override
    public void init() {
        handlers = new MessageHandler[size];
        super.init();
    }

    public void setSize(int size) {
        if (size <= 0) {
            throw new NullPointerException("size <= 0");
        }
        if (this.size > 0) {
            throw new IllegalStateException("size can't change once set.");
        }
        this.size = size;
    }

    @Override
    public <H extends MessageHandler<?, ?>> H getMessageHandler(int msgType) {
        if (msgType < 0 || msgType >= handlers.length) {
            return null;
        }
        @SuppressWarnings("unchecked")
        H handler = (H) handlers[msgType];
        return handler;
    }

    @Override
    protected void registHandler(MessageMeta meta, MessageHandler<?, ?> handler) {
        handlers[meta.getType()] = handler;
    }

    @Override
    protected void onFinishRegist() {

    }

}
