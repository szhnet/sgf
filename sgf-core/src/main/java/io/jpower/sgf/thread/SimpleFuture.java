package io.jpower.sgf.thread;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 表示一个异步的操作结果
 * <p>
 * <ul>
 * <li>类似jdk中的Future，netty中的ChannelFuture</li>
 * </ul>
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public interface SimpleFuture<V> {

    /**
     * 是否执行成功
     *
     * @return
     */
    boolean isSuccess();

    /**
     * 设置操作结果
     *
     * @param value
     * @return
     */
    boolean setSuccess(V value);

    /**
     * 返回异常
     *
     * @return 如果没有返回null
     */
    Throwable getCause();

    /**
     * 设置操作异常
     *
     * @param cause
     * @return
     */
    boolean setFailure(Throwable cause);

    /**
     * 是否取消操作
     *
     * @return
     */
    boolean isCancelled();

    /**
     * 取消操作
     *
     * @param mayInterruptIfRunning 是否可以interrupt执行操作的线程
     * @return
     */
    boolean cancel(boolean mayInterruptIfRunning);

    /**
     * 任务完成返回true，设置结果，异常，或者取消都算做完成任务。
     *
     * @return
     */
    boolean isDone();

    /**
     * 获得结果
     * <p>
     * <p>
     * 该方法不等待。如果没有结果将返回null，包括设置异常，取消都将返回null
     *
     * @return
     */
    V tryGet();

    /**
     * 获得结果
     * <p>
     * <li>该方法会等待任务完成，如果设置了结果将返回设置的结果</li>
     * <li>设置异常此方法将抛出异常</li>
     * <li>取消将抛出 {@link java.util.concurrent.CancellationException}</li>
     * <li>如果线程被interrupt将抛出{@link InterruptedException}</li>
     *
     * @return
     * @throws InterruptedException
     */
    V get() throws InterruptedException;

    /**
     * 获得结果
     * <p>
     * <li>该方法会等待任务完成，如果设置了结果将返回设置的结果</li>
     * <li>设置异常此方法将抛出异常</li>
     * <li>取消将抛出 {@link java.util.concurrent.CancellationException}</li>
     * <li>如果线程被interrupt将不会抛出{@link InterruptedException}</li>
     *
     * @return
     */
    V getUninterruptibly();

    /**
     * 获得结果
     * <p>
     * <li>该方法会等待任务完成，如果设置了结果将返回设置的结果</li>
     * <li>设置异常此方法将抛出异常</li>
     * <li>取消将抛出 {@link java.util.concurrent.CancellationException}</li>
     * <li>该方法可以设置等待时间，如果等待时间到将抛出{@link TimeoutException}</li>
     * <li>如果线程被interrupt将不会抛出{@link InterruptedException}</li>
     *
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     * @throws TimeoutException
     */
    V get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;

    /**
     * 获得结果
     * <p>
     * <li>该方法会等待任务完成，如果设置了结果将返回设置的结果</li>
     * <li>设置异常此方法将抛出异常</li>
     * <li>取消将抛出 {@link java.util.concurrent.CancellationException}</li>
     * <li>该方法可以设置等待时间，如果等待时间到将抛出{@link TimeoutException}</li>
     * <li>如果线程被interrupt将不会抛出{@link InterruptedException}</li>
     *
     * @param timeoutMillis 超时时间(ms)
     * @return
     * @throws InterruptedException
     * @throws TimeoutException
     */
    V get(long timeoutMillis) throws InterruptedException, TimeoutException;

    /**
     * 等待任务完成
     * <p>
     * <li>如果线程被interrupt将不会抛出{@link InterruptedException}</li>
     *
     * @throws InterruptedException
     */
    SimpleFuture<V> await() throws InterruptedException;

    /**
     * 等待任务完成
     * <p>
     * <li>如果线程被interrupt将不会抛出{@link InterruptedException}</li>
     */
    SimpleFuture<V> awaitUninterruptibly();

    /**
     * 等待任务完成
     * <p>
     * <li>该方法可以设置等待时间，如果等待时间到将返回false</li>
     * <li>如果线程被interrupt将会抛出 {@link InterruptedException}</li>
     *
     * @param timeout
     * @param unit
     * @return 如果等待时间内任务完成返回true，否则返回false
     * @throws InterruptedException
     */
    boolean await(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * 等待任务完成
     * <p>
     * <li>该方法可以设置等待时间，如果等待时间到将返回false</li>
     * <li>如果线程被interrupt将会抛出 {@link InterruptedException}</li>
     *
     * @param timeoutMillis 超时时间(ms)
     * @return 如果等待时间内任务完成返回true，否则返回false
     * @throws InterruptedException
     */
    boolean await(long timeoutMillis) throws InterruptedException;

}
