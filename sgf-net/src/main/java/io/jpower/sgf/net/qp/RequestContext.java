package io.jpower.sgf.net.qp;

import io.jpower.sgf.thread.DefaultSimpleFuture;
import io.jpower.sgf.thread.SimpleFuture;

/**
 * @param <Q>
 * @param <P>
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class RequestContext<Q, P> {

    private final int requestId;

    private final long timestamp;

    private final RequestCallback<Q, P> callback;

    private SimpleFuture<P> future;

    private Q requestMessage;

    public RequestContext(int requestId, RequestCallback<Q, P> callback) {
        this(requestId, callback, new DefaultSimpleFuture<P>());
    }

    public RequestContext(int requestId, RequestCallback<Q, P> callback, SimpleFuture<P> future) {
        this.requestId = requestId;
        this.timestamp = System.currentTimeMillis();
        this.callback = callback;
        this.future = future;
    }

    public int getRequestId() {
        return requestId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public RequestCallback<Q, P> getCallback() {
        return callback;
    }

    public SimpleFuture<P> getFuture() {
        return future;
    }

    public Q getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(Q requestMessage) {
        this.requestMessage = requestMessage;
    }

    public P getResponseMessage() {
        return future.tryGet();
    }

    public void setResponseMessage(P responseMessage) {
        future.setSuccess(responseMessage);
    }

}
