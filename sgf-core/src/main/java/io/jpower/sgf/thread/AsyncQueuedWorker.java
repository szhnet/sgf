package io.jpower.sgf.thread;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import io.jpower.sgf.utils.JavaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用来异步处理任务，比如发送日志
 * <p>
 * 两个处理条件：1.队列长度 2.时间间隔
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public abstract class AsyncQueuedWorker<T> implements Runnable {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public static final int DEFAULT_SHUTDOWN_WAIT_TIME = 5000;

    private static final int STATE_INIT = 0;

    private static final int STATE_START = 1;

    private static final int STATE_STOP = 2;

    private ExecutorService exec = Executors.newSingleThreadExecutor();

    private volatile int state = STATE_INIT;

    private String name;

    private int shutdownWaitTime;

    private volatile Thread workerThread;

    /**
     * 当达到此队列长度时，进行任务处理
     */
    private final int handleQueueSize;

    /**
     * 当达到此处理时间间隔，进行任务处理(ns)
     */
    private final long handleInterval;

    /**
     * 队列最大长度
     */
    private final int maxQueueSize;

    private ConcurrentLinkedQueue<T> taskQueue = new ConcurrentLinkedQueue<T>();

    private final AtomicInteger taskQueueSize = new AtomicInteger();

    /**
     * @param handleQueueSize  触发处理队列的长度
     * @param maxQueueSize     最大队列长度
     * @param handleIntervalMs 触发处理队列的时间（单位 ms）
     */
    protected AsyncQueuedWorker(int handleQueueSize, int maxQueueSize, long handleIntervalMs) {
        this.name = JavaUtils.getSimpleName(getClass());
        this.shutdownWaitTime = DEFAULT_SHUTDOWN_WAIT_TIME;
        if (handleQueueSize <= 0) {
            throw new IllegalArgumentException("handleQueueSize <= 0");
        }
        if (handleIntervalMs < 0) {
            throw new IllegalArgumentException("handleIntervalMs < 0");
        }
        this.handleQueueSize = handleQueueSize;
        this.maxQueueSize = maxQueueSize;
        this.handleInterval = TimeUnit.MILLISECONDS.toNanos(handleIntervalMs); // 转成ns
    }

    protected AsyncQueuedWorker(String name, int shutdownWaitTime, int handleQueueSize,
                                int maxQueueSize, long handleIntervalMs) {
        this.name = name;
        this.shutdownWaitTime = shutdownWaitTime;
        if (handleQueueSize <= 0) {
            throw new IllegalArgumentException("handleQueueSize <= 0");
        }
        if (handleIntervalMs < 0) {
            throw new IllegalArgumentException("handleIntervalMs < 0");
        }
        this.handleQueueSize = handleQueueSize;
        this.maxQueueSize = maxQueueSize;
        this.handleInterval = TimeUnit.MILLISECONDS.toNanos(handleIntervalMs); // 转成ns
    }

    public void start() {
        if (state != STATE_INIT) {
            throw new IllegalStateException(
                    name + " state is illegal. expected=" + STATE_INIT + ", actual=" + state);
        }

        this.state = STATE_START;

        exec.execute(new NamedRunable(name, this));

        if (log.isInfoEnabled()) {
            log.info("{} is started", name);
        }
    }

    public void stop() {
        if (this.state != STATE_START) {
            return;
        }
        this.state = STATE_STOP;

        singal();
        try {
            ExecutorUtils.terminate(shutdownWaitTime, TimeUnit.MILLISECONDS, exec);
        } catch (Exception e) {
            log.error("Failed to stop {}", name, e);
        }

        if (log.isInfoEnabled()) {
            log.info("{} is stopped", name);
        }
    }

    public void add(T task) {
        if (maxQueueSize > 0) {
            int curSize = taskQueueSize.get();
            if (curSize >= maxQueueSize) {
                reject(task);
                return;
            }
        }
        taskQueue.add(task);
        int size = taskQueueSize.incrementAndGet();
        int previousSize = size - 1;
        if (previousSize < handleQueueSize && size >= handleQueueSize) {
            singal();
        }
    }

    public void singal() {
        LockSupport.unpark(this.workerThread);
    }

    @Override
    public void run() {
        this.workerThread = Thread.currentThread();
        boolean active = true;
        long lastTryHandleTime = System.nanoTime(); // 最后尝试处理任务的时间
        boolean interrupt = false;
        while (active) {
            try {
                long lastTime = System.nanoTime();
                long waitTime = lastTryHandleTime + handleInterval - lastTime;

                int handleCount = -1;
                int queueSize = taskQueueSize.get();
                while (queueSize < handleQueueSize && waitTime > 0 && active) { // 当队列数量符合条件；到达等待时间；被关闭（用来将关闭前剩余的日志发送）时尝试处理任务
                    LockSupport.parkNanos(waitTime);

                    active = isActive();
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    long now = System.nanoTime();
                    waitTime -= now - lastTime;
                    lastTime = now;
                    queueSize = taskQueueSize.get();
                }

                handleCount = queueSize;
                lastTryHandleTime = System.nanoTime();
                if (handleCount > 0) {
                    taskQueueSize.addAndGet(-handleCount);
                    handleTask(taskQueue, handleCount);
                }
            } catch (InterruptedException e) {
                interrupt = true;
            } catch (Throwable e) {
                log.error("{} throws exception", name, e);
            }
        }

        if (interrupt) {
            Thread.currentThread().interrupt(); // re interrupt
        }
    }

    protected boolean isActive() {
        return state == STATE_START;
    }

    protected void handleTask(ConcurrentLinkedQueue<T> taskQueue, int handleCount) {
        T task = null;
        for (int i = 0; i < handleCount && (task = taskQueue.poll()) != null; i++) {
            try {
                handleTask(task);
            } catch (Throwable e) {
                exceptionCaught(task, e);
            }
        }
    }

    protected abstract void handleTask(T task);

    protected void exceptionCaught(T task, Throwable e) {
        log.error("{} handle task throws exception", name, e);
    }

    protected void reject(T task) {
        log.error("{} reject task: {}", name, task);
    }

}
