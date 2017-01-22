package io.jpower.sgf.net.msg;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

import io.jpower.sgf.net.msg.test.CgMockLoginHandler;

/**
 * SpringMessageHandler的单元测试基类
 *
 * @author zheng.sun
 */
public abstract class AbstractSpringMessageHandlerFactoryTest {

    @Test
    public void testGetMessageHandler() {
        DefaultMessageConfigManager msgConfigMgr = new DefaultMessageConfigManager(
                "MessageConfig.xml", "io.jpower.sgf.net.msg");
        msgConfigMgr.init();

        CgMockLoginHandler rstHandler = new CgMockLoginHandler();

        ApplicationContext appCtx = createMock(ApplicationContext.class);
        expect(appCtx.getBean(CgMockLoginHandler.class)).andReturn(rstHandler);

        replay(appCtx);

        MessageHandlerFactory fac = createMessageHandlerFactory(appCtx, msgConfigMgr);

        int msgType = msgConfigMgr.getMessageType(CgMockLogin.class);
        MessageHandler<?, ?> handler = fac.getMessageHandler(msgType);

        assertSame(rstHandler, handler);

        verify(appCtx);

        // 测一个不存在的情况
        assertNull(fac.getMessageHandler(msgType + 1));
    }

    protected abstract MessageHandlerFactory createMessageHandlerFactory(ApplicationContext appCtx,
                                                                         MessageConfigManager<MessageConfig.MessageMeta> msgConfigMgr);

}
