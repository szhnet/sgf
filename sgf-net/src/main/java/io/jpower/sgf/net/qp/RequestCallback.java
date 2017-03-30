package io.jpower.sgf.net.qp;

import io.jpower.sgf.net.NetSession;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public interface RequestCallback<Q, P> {

    void callback(NetSession session, RequestContext<Q, P> ctx);

}
