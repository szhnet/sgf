package io.jpower.sgf.net.qp;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class Request<Q> {

    private final int requestId;

    private Q message;

    public Request(int requestId, Q message) {
        this.requestId = requestId;
        this.message = message;
    }

    public int getRequestId() {
        return requestId;
    }

    public Q getMessage() {
        return message;
    }

    public void setMessage(Q message) {
        this.message = message;
    }

}
