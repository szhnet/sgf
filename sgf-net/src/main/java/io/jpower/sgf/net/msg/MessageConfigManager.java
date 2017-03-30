package io.jpower.sgf.net.msg;

import java.util.Collection;

/**
 * 消息配置管理器
 * <p>
 * <p>
 * 用来提供消息定义的相关数据
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public interface MessageConfigManager<T extends MessageConfig.MessageMeta> {

    /**
     * 根据消息体的class获得消息的type
     *
     * @param bodyClazz
     * @return 返回相应的type，如果没有返回-1
     */
    int getMessageType(Class<?> bodyClazz);

    /**
     * 根据消息的type获得消息体的class
     *
     * @param msgType
     * @return
     */
    <C> Class<C> getMessageBodyClass(int msgType);

    /**
     * 根据消息的type获得消息的配置meta
     * <p>
     * <p>
     * 如果{@link T}不能满足需要，可以在{@link T}的基础上进行继承
     *
     * @param msgType
     * @return
     */
    T getMessageMeta(int msgType);

    Collection<T> getMessageMetas();

}
