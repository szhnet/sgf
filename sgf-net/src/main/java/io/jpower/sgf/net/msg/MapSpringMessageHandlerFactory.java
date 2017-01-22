package io.jpower.sgf.net.msg;

import java.util.HashMap;
import java.util.Map;

import io.jpower.sgf.net.msg.MessageConfig.MessageMeta;

/**
 * 内部用一个HashMap来保存handler
 *
 * @author zheng.sun
 */
public class MapSpringMessageHandlerFactory extends AbstractSpringMessageHandlerFactory {

    private Map<Integer, MessageHandler<?, ?>> handlerMap = new HashMap<Integer, MessageHandler<?, ?>>();

    @Override
    public <H extends MessageHandler<?, ?>> H getMessageHandler(int msgType) {
        @SuppressWarnings("unchecked")
        H handler = (H) handlerMap.get(msgType);
        return handler;
    }

    @Override
    protected void registHandler(MessageMeta meta, MessageHandler<?, ?> handler) {
        handlerMap.put(meta.getType(), handler);
    }

    @Override
    protected void onFinishRegist() {

    }

}
