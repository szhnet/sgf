package io.jpower.sgf.thread;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

import io.jpower.sgf.utils.JavaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用单线程来执行某些任务
 *
 * @param <T>
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public abstract class SingleThreadTaskWorker<T> implements Runnable {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final int STATE_INIT = 0;

    private static final int STATE_START = 1;

    private static final int STATE_STOP = 2;

    private ExecutorService exec = Executors.newSingleThreadExecutor();

    private volatile int state = STATE_INIT;

    private String name;

    private int shutdownWaitTime = 5000;

    private volatile Thread workerThread;

    private AtomicBoolean notified = new AtomicBoolean();

    private ConcurrentLinkedQueue<T> taskQueue = new ConcurrentLinkedQueue<>();

    protected SingleThreadTaskWorker() {
        this.name = JavaUtils.getSimpleName(getClass());
    }

    protected SingleThreadTaskWorker(String name) {
        this.name = name;
    }

    protected SingleThreadTaskWorker(int shutdownWaitTime) {
        this.name = JavaUtils.getSimpleName(getClass());
        this.shutdownWaitTime = shutdownWaitTime;
    }

    protected SingleThreadTaskWorker(String name, int shutdownWaitTime) {
        this.name = name;
        this.shutdownWaitTime = shutdownWaitTime;
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

    public boolean isWorkerThread() {
        return this.workerThread == Thread.currentThread();
    }

    @Override
    public void run() {
        Thread workerThread = Thread.currentThread();
        this.workerThread = workerThread;
        beforeMainLoop();

        boolean interrupt = false;
        while (this.state == STATE_START) {
            while (!notified.get() && this.state == STATE_START) { // 条件！
                LockSupport.park();
            }

            try {
                notified.set(false);
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                T task = null;
                while ((task = taskQueue.poll()) != null) {
                    try {
                        execute(task);
                    } catch (Throwable e) {
                        log.error("{} throws exception", name, e);
                    }
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

        afterMainLoop();
    }

    private void singal() {
        LockSupport.unpark(this.workerThread);
    }

    protected void addTask(T task) {
        taskQueue.add(task);
        if (this.state == STATE_START) {
            if (!notified.get() && notified.compareAndSet(false, true)) {
                singal();
            }
        } else {
            if (taskQueue.remove(task)) {
                throw new RejectedExecutionException("Worker has already been shutdown");
            }
        }
    }

    /**
     * 在开始主循环之前调用
     */
    protected void beforeMainLoop() {

    }

    /**
     * 在结束主循环之前调用
     */
    protected void afterMainLoop() {

    }

    protected abstract void execute(T task);

}
