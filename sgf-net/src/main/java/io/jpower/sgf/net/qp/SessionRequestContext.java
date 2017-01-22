package io.jpower.sgf.net.qp;

import io.jpower.sgf.net.ShareChannelSession;

/**
 * @author zheng.sun
 */
public class SessionRequestContext {

    private final ShareChannelSession session;

    private final RequestContext<?, ?> reqeustContext;

    public SessionRequestContext(ShareChannelSession session, RequestContext<?, ?> reqeustContext) {
        this.session = session;
        this.reqeustContext = reqeustContext;
    }

    public ShareChannelSession getSession() {
        return session;
    }

    public RequestContext<?, ?> getReqeustContext() {
        return reqeustContext;
    }

}
