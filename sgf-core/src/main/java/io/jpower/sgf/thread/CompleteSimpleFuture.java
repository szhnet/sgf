package io.jpower.sgf.thread;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 用来表示一个已经完成的future
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public abstract class CompleteSimpleFuture<V> implements SimpleFuture<V> {

    @Override
    public SimpleFuture<V> addListener(SimpleFutureListener<? extends SimpleFuture<? super V>> listener) {
        Objects.requireNonNull(listener, "listener");
        DefaultSimpleFuture.notifyListener(this, listener);
        return this;
    }

    @Override
    public SimpleFuture<V> addListeners(SimpleFutureListener<? extends SimpleFuture<? super V>>[] listeners) {
        Objects.requireNonNull(listeners, "listeners");
        for (SimpleFutureListener<? extends SimpleFuture<? super V>> l : listeners) {
            if (l == null) {
                break;
            }
            DefaultSimpleFuture.notifyListener(this, l);
        }
        return this;
    }

    @Override
    public SimpleFuture<V> removeListener(SimpleFutureListener<? extends SimpleFuture<? super V>> listener) {
        // NOOP
        return this;
    }

    @Override
    public SimpleFuture<V> removeListeners(SimpleFutureListener<? extends SimpleFuture<? super V>>[] listeners) {
        // NOOP
        return this;
    }

    @Override
    public SimpleFuture<V> setSuccess(V result) {
        throw new IllegalStateException("complete already: " + this);
    }

    @Override
    public boolean trySuccess(V result) {
        return false;
    }

    @Override
    public SimpleFuture<V> setFailure(Throwable cause) {
        throw new IllegalStateException("complete already: " + this);
    }

    @Override
    public boolean tryFailure(Throwable cause) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public SimpleFuture<V> await() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        return this;
    }

    @Override
    public SimpleFuture<V> awaitUninterruptibly() {
        return this;
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        return true;
    }

    @Override
    public boolean await(long timeoutMillis) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        return true;
    }

}
