package io.jpower.sgf.thread;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.jpower.sgf.utils.JavaUtils;

/**
 * 用来表示一个执行失败的future
 *
 * @param <V>
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
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
        throw JavaUtils.sneakyThrow(new ExecutionException(cause));
    }

    @Override
    public V getUninterruptibly() {
        throw JavaUtils.sneakyThrow(new ExecutionException(cause));
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        throw JavaUtils.sneakyThrow(new ExecutionException(cause));
    }

    @Override
    public V get(long timeoutMillis) throws InterruptedException, TimeoutException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        throw JavaUtils.sneakyThrow(new ExecutionException(cause));
    }

}
