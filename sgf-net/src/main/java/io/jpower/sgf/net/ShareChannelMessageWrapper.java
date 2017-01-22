package io.jpower.sgf.net;

/**
 * @author zheng.sun
 */
public class ShareChannelMessageWrapper {

    private final ShareChannelSession session;

    private final Object message;

    public ShareChannelMessageWrapper(ShareChannelSession session, Object message) {
        this.session = session;
        this.message = message;

    }

    public ShareChannelSession getSession() {
        return session;
    }

    public Object getMessage() {
        return message;
    }

}
