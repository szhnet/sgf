package io.jpower.sgf.net.msg;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 内部约定了一些自动查找{@link MessageHandler}的规则
 * <p>
 * <ul>
 * <li>handler所在的位置：规定了所有的handler都在一个包或者子包下面</li>
 * <li>handler的命名规则：handler的Class名称是消息的名字加上Handler后缀。 例如消息的名字为
 * <code>CgLogin</code>，那么该消息对应的handler的Class名称为<code>CgLoginHandler</code></li>
 * </ul>
 *
 * @author zheng.sun
 */
public abstract class AbstractMessageHandlerFactory implements MessageHandlerFactory {

    public static final String HANDLER_SUFFIX = "Handler";

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected MessageConfigManager<MessageConfig.MessageMeta> messageConfigManager;

    protected String handlerParentPackage;

    protected AbstractMessageHandlerFactory() {

    }

    /**
     * @param messageConfigManager
     * @param handlerParentPackage 包名，handler需要在此包或者此包的子包下面
     */
    protected AbstractMessageHandlerFactory(MessageConfigManager<MessageConfig.MessageMeta> messageConfigManager,
                                            String handlerParentPackage) {
        setMessageConfigManager(messageConfigManager);
        setHandlerParentPackage(handlerParentPackage);
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

    /**
     * @param handlerParentPackage 包名，handler需要在此包或者此包的子包下面
     */
    public void setHandlerParentPackage(String handlerParentPackage) {
        if (handlerParentPackage == null) {
            throw new NullPointerException("handlerParentPackage");
        }
        if (this.handlerParentPackage != null) {
            throw new IllegalStateException("handlerParentPackage can't change once set.");
        }
        this.handlerParentPackage = handlerParentPackage;
    }

    /**
     * 初始化，进行handler加载注册等等
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void init() {
        Reflections reflections = new Reflections(this.handlerParentPackage);
        Set<Class<? extends MessageHandler>> handlerClazzes = reflections
                .getSubTypesOf(MessageHandler.class); // 找到包下的所有类
        Map<String, Class<? extends MessageHandler>> nameClazzMap = new HashMap<String, Class<? extends MessageHandler>>();
        for (Class<? extends MessageHandler> clazz : handlerClazzes) { // 转成用SimpleName做key，方便查找
            nameClazzMap.put(clazz.getSimpleName(), clazz);
        }

        int handlerNum = 0;
        for (MessageConfig.MessageMeta msgMeta : messageConfigManager.getMessageMetas()) {
            if (!isSupported(msgMeta)) {
                continue;
            }
            String clazzSimpleName = msgMeta.getName() + HANDLER_SUFFIX; // handler类的规则，名字+Handler，例如CgLoginHandler
            Class<? extends MessageHandler> handlerClazz = nameClazzMap.get(clazzSimpleName);
            if (handlerClazz == null) {
                log.error(
                        "Not found Handler Class for message. messageName={}, handlerClass simpleName={}",
                        msgMeta.getName(), clazzSimpleName);
                continue;
            }
            MessageHandler<?, ?> handler = getHandlerInstance(msgMeta,
                    (Class<? extends MessageHandler<?, ?>>) handlerClazz);
            if (handler == null) {
                log.error(
                        "Not found Handler instant for message. messageName=" + msgMeta.getName());
                continue;
            }
            registHandler(msgMeta, handler);
            handlerNum++;
        }

        if (log.isInfoEnabled()) {
            log.info("{} initialized {} handlers.", this.getClass(), handlerNum);
        }
        onFinishRegist();
    }

    /**
     * 是否支持此消息，子类可以根据自己的规则覆盖
     *
     * @param meta
     * @return
     */
    protected boolean isSupported(MessageConfig.MessageMeta meta) {
        return true;
    }

    /**
     * 获得handler的实例，具体创建实例的方式由子类确定
     *
     * @param meta
     * @param clazz handler的Class
     * @return
     */
    protected abstract MessageHandler<?, ?> getHandlerInstance(MessageConfig.MessageMeta meta,
                                                               Class<? extends MessageHandler<?, ?>> clazz);

    /**
     * 注册一个handler
     *
     * @param meta
     * @param handler
     */
    protected abstract void registHandler(MessageConfig.MessageMeta meta, MessageHandler<?, ?> handler);

    /**
     * 当所有handler注册完成后触发，可以用来做一些后续处理
     */
    protected abstract void onFinishRegist();

}
