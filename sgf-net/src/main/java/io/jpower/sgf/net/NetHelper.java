package io.jpower.sgf.net;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
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
