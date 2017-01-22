package io.jpower.sgf.net;

/**
 * @author zheng.sun
 */
public class NetHelper {

    public static String getSessionDesc(NetSession session) {
        Object attachment = session.getAttachment();
        if (attachment instanceof BizSession) {
            return attachment.toString();
        } else {
            return session.toString();
        }
    }

}
