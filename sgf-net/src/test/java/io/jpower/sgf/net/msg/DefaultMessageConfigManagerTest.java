package io.jpower.sgf.net.msg;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class DefaultMessageConfigManagerTest {

    DefaultMessageConfigManager mgr;

    @Before
    public void setUp() throws Exception {
        mgr = new DefaultMessageConfigManager("MessageConfig.xml",
                "io.jpower.sgf.net.msg");
        mgr.init();
    }

    @Test
    public void testGetMessageType() {
        assertEquals(1, mgr.getMessageType(CgMockLogin.class));
        assertEquals(2, mgr.getMessageType(GcMockLoginInfo.class));
        assertEquals(3, mgr.getMessageType(CgMockCreateRole.class));

        // 测一个不存在的
        assertEquals(-1, mgr.getMessageType(CgMockOnLoad.class));
    }

    @Test
    public void testGetMessageBodyClass() {
        assertEquals(CgMockLogin.class, mgr.getMessageBodyClass(1));
        assertEquals(GcMockLoginInfo.class, mgr.getMessageBodyClass(2));
        assertEquals(CgMockCreateRole.class, mgr.getMessageBodyClass(3));

        // 测一个不存在的
        assertNull(mgr.getMessageBodyClass(100));
    }

    @Test
    public void testGetMessageMeta() {
        MessageConfig.MessageMeta msgMeta = mgr.getMessageMeta(1);
        assertEquals(1, msgMeta.getType());
        assertEquals("CgMockLogin", msgMeta.getName());

        // 测一个不存在的
        assertNull(mgr.getMessageMeta(100));
    }

    @Test
    public void testGetMessageMetas() {
        Collection<? extends MessageConfig.MessageMeta> msgMetas = mgr.getMessageMetas();
        assertEquals(3, msgMetas.size());
    }

}
