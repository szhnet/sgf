package io.jpower.sgf.thread;

import java.util.concurrent.TimeUnit;

/**
 * 用来表示一个已经完成的future
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public abstract class CompleteSimpleFuture<V> implements SimpleFuture<V> {

    @Override
    public boolean setSuccess(V value) {
        return false;
    }

    @Override
    public boolean setFailure(Throwable cause) {
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
