package io.jpower.sgf.net.msg;

/**
 * @author zheng.sun
 */
public interface MessageDispatcher {

    void dispatch(IMessage message, Object context);

}
