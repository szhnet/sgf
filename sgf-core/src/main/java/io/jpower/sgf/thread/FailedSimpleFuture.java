package io.jpower.sgf.thread;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 用来表示一个执行失败的future
 *
 * @param <V>
 * @author zheng.sun
 */
public class FailedSimpleFuture<V> extends CompleteSimpleFuture<V> {

    private final Throwable cause;

    public FailedSimpleFuture(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    @Override
    public V tryGet() {
        return null;
    }

    @Override
    public V get() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        return null;
    }

    @Override
    public V getUninterruptibly() {
        return null;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        return null;
    }

    @Override
    public V get(long timeoutMillis) throws InterruptedException, TimeoutException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        return null;
    }

}
