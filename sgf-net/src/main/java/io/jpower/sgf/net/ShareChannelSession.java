package io.jpower.sgf.net;

import java.net.InetSocketAddress;

import io.jpower.sgf.net.qp.RequestCallback;
import io.jpower.sgf.net.qp.RequestContext;
import io.jpower.sgf.net.qp.SessionRequestContext;
import io.jpower.sgf.thread.FailedSimpleFuture;

/**
 * @author zheng.sun
 */
public class ShareChannelSession extends NetSession {

    private InetSocketAddress remoteAddress;

    public ShareChannelSession(int id) {
        super(id);
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    public void send(Object msg) {
        if (channel != null && channel.isConnected()) {
            ShareChannelMessageWrapper smsg = new ShareChannelMessageWrapper(this, msg);
            channel.write(smsg);
        }
    }

    @Override
    public <Q, P> RequestContext<Q, P> request(Q msg, RequestCallback<Q, P> callback) {
        RequestContext<Q, P> ctx = null;
        if (channel != null && channel.isConnected()) {
            ctx = new RequestContext<Q, P>(requestCtn.generateId(), callback);
            ctx.setRequestMessage(msg);

            requestCtn.add(ctx);

            SessionRequestContext sctx = new SessionRequestContext(this, ctx);
            channel.write(sctx);
        } else {
            ctx = new RequestContext<Q, P>(requestCtn.generateId(), callback,
                    new FailedSimpleFuture<P>(new ClosedSessionException()));
        }
        return ctx;
    }

    @Override
    public void close() {

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append("ShareChannelSession(id=").append(id).append(", remoteAddress=");
        InetSocketAddress remoteAddress = getRemoteAddress();
        if (remoteAddress != null) {
            sb.append(remoteAddress);
        } else {
            sb.append("unknow");
        }
        sb.append(")");
        return sb.toString();
    }

}
