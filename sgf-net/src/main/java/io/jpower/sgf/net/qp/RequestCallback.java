package io.jpower.sgf.net.qp;

import io.jpower.sgf.net.NetSession;

/**
 * @author zheng.sun
 */
public interface RequestCallback<Q, P> {

    void callback(NetSession session, RequestContext<Q, P> ctx);

}
