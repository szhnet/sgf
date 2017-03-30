package io.jpower.sgf.net.qp;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.TimerTask;

import io.jpower.sgf.net.NetSession;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class QpMonitor {

    private static final int STATE_INIT = 0;

    private static final int STATE_START = 1;

    private static final int STATE_STOP = 2;

    private volatile int state = STATE_INIT;

    private int timeout;

    private ConcurrentMap<NetSession, Timeout> sessions;

    private HashedWheelTimer timer;

    private long timeDelay;

    public QpMonitor(int timeout) {
        this(timeout, 1000);
    }

    public QpMonitor(int timeout, long timeDelay) {
        this.timeout = timeout;
        this.sessions = new ConcurrentHashMap<NetSession, Timeout>();
        this.timer = new HashedWheelTimer();
    }

    public void start() {
        if (state != STATE_INIT) {
            throw new IllegalStateException("QpMonitor state is not INIT");
        }

        state = STATE_START;
    }

    public void stop() {
        if (state != STATE_START) {
            throw new IllegalStateException("QpMonitor state is not START");
        }

        state = STATE_STOP;

        this.timer.stop();
        this.sessions.clear();
    }

    public void add(NetSession session) {
        if (state != STATE_START) {
            throw new IllegalStateException("SceneScheduler state is not START");
        }
        Timeout tout = timer.newTimeout(new CheckTask(session), timeDelay, TimeUnit.MILLISECONDS);
        Timeout etout = sessions.putIfAbsent(session, tout);
        if (etout != null) {
            tout.cancel();
            throw new IllegalStateException("session duplicate. exists=" + session);
        }
    }

    public void remove(NetSession session) {
        Timeout tout = sessions.remove(session);
        if (tout == null) {
            throw new IllegalStateException("session not exists. session=" + session);
        }
        tout.cancel();
    }

    private class CheckTask implements TimerTask {

        private NetSession session;

        public CheckTask(NetSession session) {
            this.session = session;
        }

        @Override
        public void run(Timeout to) {
            long now = System.currentTimeMillis();
            TimeoutException timeExp = new TimeoutException();
            RequestContainer requestCtn = session.getRequestContainer();
            Iterator<RequestContext<?, ?>> itr = requestCtn.requestIterator();
            while (itr.hasNext()) {
                RequestContext<?, ?> req = itr.next();
                if (now - req.getTimestamp() > timeout) {
                    req.getFuture().setFailure(timeExp);
                }
            }
        }

    }

}
