package io.jpower.sgf.net.msg;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 使用Spring IOC来进行handler管理
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public abstract class AbstractSpringMessageHandlerFactory extends AbstractMessageHandlerFactory
        implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    protected MessageHandler<?, ?> getHandlerInstance(MessageConfig.MessageMeta meta,
                                                      Class<? extends MessageHandler<?, ?>> clazz) {
        return (MessageHandler<?, ?>) applicationContext.getBean(clazz);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
