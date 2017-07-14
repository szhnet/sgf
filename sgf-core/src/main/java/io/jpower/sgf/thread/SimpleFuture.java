package io.jpower.sgf.thread;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
public interface SimpleFuture<V> extends Future<V> {

    /**
     * 添加指定的listener到此future，当此future{@linkplain #isDone() 完成}时，listener将被通知。
     * 如果此future已经完成，future将被立即通知。
     *
     * @param listener
     * @return
     */
    SimpleFuture<V> addListener(SimpleFutureListener<? extends SimpleFuture<? super V>> listener);

    /**
     * 添加指定的listeners到此future，当此future{@linkplain #isDone() 完成}时，listeners将被通知。
     * 如果此future已经完成，future将被立即通知。
     *
     * @param listeners
     * @return
     */
    SimpleFuture<V> addListeners(SimpleFutureListener<? extends SimpleFuture<? super V>>... listeners);

    /**
     * 从此future移除指定的listener，当此future{@linkplain #isDone() 完成}时，listener将不再被通知。
     * 如果指定的listener没有与此future关联，那么此方法将不做任何事。
     *
     * @param listener
     * @return
     */
    SimpleFuture<V> removeListener(SimpleFutureListener<? extends SimpleFuture<? super V>> listener);

    /**
     * 从此future移除指定的每一个listener，当此future{@linkplain #isDone() 完成}时，listeners将不再被通知。
     * 如果指定的listeners没有与此future关联，那么此方法将不做任何事。
     *
     * @param listeners
     * @return
     */
    SimpleFuture<V> removeListeners(SimpleFutureListener<? extends SimpleFuture<? super V>>... listeners);

    /**
     * 是否执行成功
     *
     * @return
     */
    boolean isSuccess();

    /**
     * 设置操作结果，并通知所有listener
     *
     * <p>如果future已经完成，将抛出异常{@link IllegalStateException}.
     *
     * @param result
     * @return
     */
    SimpleFuture<V> setSuccess(V result);

    /**
     * 设置操作结果，并通知所有listener
     *
     * @param result
     * @return
     */
    boolean trySuccess(V result);

    /**
     * 返回异常
     *
     * @return 如果没有返回null
     */
    Throwable getCause();

    /**
     * 设置操作异常
     *
     * <p>如果future已经完成，将抛出异常{@link IllegalStateException}.
     *
     * @param cause
     * @return
     */
    SimpleFuture<V> setFailure(Throwable cause);

    /**
     * 设置操作异常
     *
     * @param cause
     * @return
     */
    boolean tryFailure(Throwable cause);

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
     * <li>取消将抛出 {@link CancellationException}</li>
     * <li>任务出错将抛出 {@link ExecutionException}</li>
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
     * <li>取消将抛出 {@link CancellationException}</li>
     * <li>任务出错将抛出 {@link ExecutionException}</li>
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
     * <li>取消将抛出 {@link CancellationException}</li>
     * <li>任务出错将抛出 {@link ExecutionException}</li>
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
     * <li>取消将抛出 {@link CancellationException}</li>
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
