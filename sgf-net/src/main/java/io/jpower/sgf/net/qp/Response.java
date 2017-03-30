package io.jpower.sgf.net.qp;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class Response<P> {

    private final int requestId;

    private P message;

    public Response(int requestId, P message) {
        this.requestId = requestId;
        this.message = message;
    }

    public int getRequestId() {
        return requestId;
    }

    public P getMessage() {
        return message;
    }

    public void setMessage(P message) {
        this.message = message;
    }

}
