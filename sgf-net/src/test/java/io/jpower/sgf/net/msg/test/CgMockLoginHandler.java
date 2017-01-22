package io.jpower.sgf.net.msg.test;

import io.jpower.sgf.net.NetSession;
import io.jpower.sgf.net.msg.CgMockLogin;
import io.jpower.sgf.net.msg.MessageHandler;

/**
 * 用于辅助单元测试
 *
 * @author zheng.sun
 */
public class CgMockLoginHandler implements MessageHandler<CgMockLogin, NetSession> {

    @Override
    public void handle(CgMockLogin message, NetSession session) {

    }

}
