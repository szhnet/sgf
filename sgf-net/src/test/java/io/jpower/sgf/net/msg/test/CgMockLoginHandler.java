package io.jpower.sgf.net.msg.test;

import io.jpower.sgf.net.NetSession;
import io.jpower.sgf.net.msg.CgMockLogin;
import io.jpower.sgf.net.msg.MessageHandler;

/**
 * 用于辅助单元测试
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class CgMockLoginHandler implements MessageHandler<CgMockLogin, NetSession> {

    @Override
    public void handle(CgMockLogin message, NetSession session) {

    }

}
