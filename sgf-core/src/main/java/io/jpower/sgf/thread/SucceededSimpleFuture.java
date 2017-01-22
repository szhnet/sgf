package io.jpower.sgf.thread;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 用来表示一个执行操作成功的future
 *
 * @param <V>
 * @author zheng.sun
 */
public class SucceededSimpleFuture<V> extends CompleteSimpleFuture<V> {

    private final V result;

    public SucceededSimpleFuture(V result) {
        this.result = result;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public Throwable getCause() {
        return null;
    }

    @Override
    public V tryGet() {
        return result;
    }

    @Override
    public V get() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        return result;
    }

    @Override
    public V getUninterruptibly() {
        return result;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        return result;
    }

    @Override
    public V get(long timeoutMillis) throws InterruptedException, TimeoutException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        return result;
    }

}
