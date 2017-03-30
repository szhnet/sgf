package io.jpower.sgf.thread;

import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import io.jpower.sgf.utils.JavaUtils;

/**
 * future的默认实现
 *
 * @param <V>
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class DefaultSimpleFuture<V> implements SimpleFuture<V> {

    private Sync sync;

    public DefaultSimpleFuture() {
        sync = new Sync();
    }

    @Override
    public boolean isSuccess() {
        return sync.innerIsSuccess();
    }

    @Override
    public boolean setSuccess(V result) {
        return sync.innerSet(result);
    }

    @Override
    public Throwable getCause() {
        return sync.innerGetThrowable();
    }

    @Override
    public boolean setFailure(Throwable cause) {
        return sync.innerSetException(cause);
    }

    @Override
    public boolean isCancelled() {
        return sync.innerIsCancelled();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return sync.innerCancel();
    }

    @Override
    public boolean isDone() {
        return sync.innerIsDone();
    }

    @Override
    public V tryGet() {
        return sync.innerGet();
    }

    @Override
    public V get() throws InterruptedException {
        return sync.innerGetSync();
    }

    @Override
    public V getUninterruptibly() {
        return sync.innerGetSyncUninterruptibly();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        return sync.innerGetSync(unit.toNanos(timeout));
    }

    @Override
    public V get(long timeoutMillis) throws InterruptedException, TimeoutException {
        return sync.innerGetSync(TimeUnit.MILLISECONDS.toNanos(timeoutMillis));
    }

    @Override
    public SimpleFuture<V> await() throws InterruptedException {
        sync.acquireSharedInterruptibly(0);
        return this;
    }

    @Override
    public SimpleFuture<V> awaitUninterruptibly() {
        sync.acquireShared(0);
        return this;
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireSharedNanos(0, unit.toNanos(timeout));
    }

    @Override
    public boolean await(long timeoutMillis) throws InterruptedException {
        return sync.tryAcquireSharedNanos(0, TimeUnit.MILLISECONDS.toNanos(timeoutMillis));
    }

    private final class Sync extends AbstractQueuedSynchronizer {

        private static final long serialVersionUID = 5353039645639466754L;

        private static final int RESULT = 1;

        private static final int CANCELLED = 2;

        private V result;

        private Throwable exception;

        private volatile boolean done;

        @Override
        protected int tryAcquireShared(int ignore) {
            return innerIsDone() ? 1 : -1;
        }

        @Override
        protected boolean tryReleaseShared(int arg) {
            done = true;
            return true;
        }

        boolean innerIsDone() {
            return done;
        }

        boolean innerIsSuccess() {
            return getState() == RESULT && done && exception == null;
        }

        boolean innerIsCancelled() {
            return getState() == CANCELLED;
        }

        V innerGet() {
            return result;
        }

        Throwable innerGetThrowable() {
            return exception;
        }

        V innerGetSync() throws InterruptedException {
            acquireSharedInterruptibly(0);
            if (getState() == CANCELLED) {
                throw new CancellationException();
            }
            if (exception != null) {
                throw JavaUtils.sneakyThrow(exception);
            }
            return result;
        }

        V innerGetSyncUninterruptibly() {
            acquireShared(0);
            if (getState() == CANCELLED) {
                throw new CancellationException();
            }
            if (exception != null) {
                throw JavaUtils.sneakyThrow(exception);
            }
            return result;
        }

        V innerGetSync(long nanosTimeout) throws InterruptedException, TimeoutException {
            if (!tryAcquireSharedNanos(0, nanosTimeout)) {
                throw new TimeoutException();
            }
            if (getState() == CANCELLED) {
                throw new CancellationException();
            }
            if (exception != null) {
                throw JavaUtils.sneakyThrow(exception);
            }
            return result;
        }

        boolean innerSet(V v) {
            for (; ; ) {
                int s = getState();
                if (s != 0) {
                    return false;
                }
                if (compareAndSetState(s, RESULT)) {
                    result = v;
                    releaseShared(0);
                    return true;
                }
            }
        }

        boolean innerSetException(Throwable t) {
            for (; ; ) {
                int s = getState();
                if (s != 0) {
                    return false;
                }
                if (compareAndSetState(s, RESULT)) {
                    exception = t;
                    result = null;
                    releaseShared(0);
                    return true;
                }
            }
        }

        boolean innerCancel() {
            for (; ; ) {
                int s = getState();
                if (s != 0) {
                    return false;
                }
                if (compareAndSetState(s, CANCELLED)) {
                    releaseShared(0);
                    return true;
                }
            }
        }

    }

}
