package io.jpower.sgf.net;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import io.jpower.sgf.net.qp.Request;
import io.jpower.sgf.net.qp.RequestCallback;
import io.jpower.sgf.net.qp.RequestContainer;
import io.jpower.sgf.net.qp.RequestContext;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;

import io.jpower.sgf.net.qp.Response;
import io.jpower.sgf.thread.FailedSimpleFuture;

/**
 * @author zheng.sun
 */
public class NetSession {

    protected int id;

    private int encodeBufferSize = 128;

    protected Channel channel;

    private Object attachment;

    protected RequestContainer requestCtn;

    private int lastReceiveSequence = -1;

    private AtomicInteger sendSequence = new AtomicInteger(0);

    public NetSession() {

    }

    public NetSession(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public int getEncodeBufferSize() {
        return encodeBufferSize;
    }

    public void setEncodeBufferSize(int encodeBufferSize) {
        this.encodeBufferSize = encodeBufferSize;
    }

    public InetSocketAddress getRemoteAddress() {
        if (channel == null) {
            return null;
        } else {
            InetSocketAddress remoteAddress = (InetSocketAddress) channel.getRemoteAddress();
            return remoteAddress;
        }
    }

    public <T> T getAttachment() {
        @SuppressWarnings("unchecked")
        T o = (T) attachment;
        return o;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    public int getLastReceiveSequence() {
        return lastReceiveSequence;
    }

    public boolean validateReceiveSequence(int sequence) {
        if (sequence < 0) {
            return false;
        }
        if (this.lastReceiveSequence == -1) {
            this.lastReceiveSequence = sequence;
            return true;
        }
        int nextSequence = this.lastReceiveSequence + 1;
        if (nextSequence > Short.MAX_VALUE) {
            nextSequence = 0;
        }
        if (nextSequence == sequence) {
            this.lastReceiveSequence = nextSequence;
            return true;
        } else {
            return false;
        }
    }

    public void send(Object msg) {
        if (channel != null && channel.isConnected()) {
            channel.write(msg);
        }
    }

    public int nextSendSequence() {
        for (; ; ) {
            int curSequence = sendSequence.get();
            int nextSequence;
            if (curSequence == Short.MAX_VALUE) {
                nextSequence = 0;
            } else {
                nextSequence = curSequence + 1;
            }
            if (sendSequence.compareAndSet(curSequence, nextSequence)) {
                return curSequence;
            }
        }
    }

    public <Q, P> RequestContext<Q, P> request(Q msg, RequestCallback<Q, P> callback) {
        RequestContext<Q, P> ctx = null;
        if (channel != null && channel.isConnected()) {
            ctx = new RequestContext<Q, P>(requestCtn.generateId(), callback);
            ctx.setRequestMessage(msg);

            requestCtn.add(ctx);

            channel.write(ctx);
        } else {
            ctx = new RequestContext<Q, P>(requestCtn.generateId(), callback,
                    new FailedSimpleFuture<P>(new ClosedSessionException()));
        }
        return ctx;
    }

    public void close() {
        if (channel == null || !channel.isConnected()) {
            return;
        }

        channel.close();
    }

    /**
     * 发送消息，然后关闭
     *
     * @param msg
     */
    public void close(Object msg) {
        if (channel == null || !channel.isConnected()) {
            return;
        }

        channel.write(msg).addListener(ChannelFutureListener.CLOSE);
    }

    public <Q, P> RequestContext<Q, P> getRequest(int requestId) {
        return requestCtn.get(requestId);
    }

    public void response(Request<?> q, Object msg) {
        Response<?> p = new Response<Object>(q.getRequestId(), msg);
        channel.write(p);
    }

    public void onRequestMode() {
        this.requestCtn = new RequestContainer();
    }

    public RequestContainer getRequestContainer() {
        return this.requestCtn;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append("NetSession(id=").append(id).append(", remoteAddress=");
        if (channel != null) {
            sb.append(channel.getRemoteAddress());
        } else {
            sb.append("unknow");
        }
        sb.append(")");
        return sb.toString();
    }

}
