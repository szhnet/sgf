package io.jpower.sgf.net.msg;

import org.springframework.context.ApplicationContext;

/**
 * @author zheng.sun
 */
public class MapSpringMessageHandlerFactoryTest extends AbstractSpringMessageHandlerFactoryTest {

    @Override
    protected MessageHandlerFactory createMessageHandlerFactory(ApplicationContext appCtx,
                                                                MessageConfigManager<MessageConfig.MessageMeta> msgConfigMgr) {
        MapSpringMessageHandlerFactory fac = new MapSpringMessageHandlerFactory() {

            @Override
            protected boolean isSupported(MessageConfig.MessageMeta meta) {
                return meta.getName().equals(CgMockLogin.class.getSimpleName()); // 这里只注册本次测试需要的handler就可以了
            }

        };
        fac.setMessageConfigManager(msgConfigMgr);
        fac.setHandlerParentPackage("io.jpower.sgf.net.msg.test");
        fac.setApplicationContext(appCtx);

        fac.init();
        return fac;
    }

}
