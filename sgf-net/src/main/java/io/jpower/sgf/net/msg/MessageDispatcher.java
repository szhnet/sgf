package io.jpower.sgf.net.msg;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public interface MessageDispatcher {

    void dispatch(IMessage message, Object context);

}
