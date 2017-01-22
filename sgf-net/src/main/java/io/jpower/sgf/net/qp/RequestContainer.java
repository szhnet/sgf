package io.jpower.sgf.net.qp;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zheng.sun
 */
public class RequestContainer {

    private AtomicInteger idGen = new AtomicInteger(1);

    private Map<Integer, RequestContext<?, ?>> map = new ConcurrentHashMap<Integer, RequestContext<?, ?>>();

    public int generateId() {
        for (; ; ) {
            int curId = idGen.get();
            int nextId;
            if (curId == Integer.MAX_VALUE) {
                nextId = 1;
            } else {
                nextId = curId + 1;
            }
            if (idGen.compareAndSet(curId, nextId)) {
                return curId;
            }
        }
    }

    public void add(RequestContext<?, ?> ctx) {
        map.put(ctx.getRequestId(), ctx);
    }

    public <Q, P> RequestContext<Q, P> get(int requestId) {
        @SuppressWarnings("unchecked")
        RequestContext<Q, P> ctx = (RequestContext<Q, P>) map.get(requestId);
        return ctx;
    }

    public Iterator<RequestContext<?, ?>> requestIterator() {
        return map.values().iterator();
    }

}
