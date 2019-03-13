package io.jpower.sgf.thread;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import io.jpower.sgf.utils.JavaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public abstract class SingleThreadWorker implements Runnable, Closeable {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final int DEFAULT_SHUTDOWN_WAIT_TIME = 5000;

    private static final int STATE_INIT = 0;

    private static final int STATE_START = 1;

    private static final int STATE_STOP = 2;

    private ExecutorService exec;

    private volatile int state = STATE_INIT;

    private String name;

    private int shutdownWaitTime;

    private volatile Thread workerThread;

    protected SingleThreadWorker() {
        this(null, DEFAULT_SHUTDOWN_WAIT_TIME, null);
    }

    protected SingleThreadWorker(String name) {
        this(name, DEFAULT_SHUTDOWN_WAIT_TIME, null);
    }

    protected SingleThreadWorker(int shutdownWaitTime) {
        this(null, shutdownWaitTime, null);
    }

    protected SingleThreadWorker(ThreadFactory threadFactory) {
        this(null, DEFAULT_SHUTDOWN_WAIT_TIME, threadFactory);
    }

    protected SingleThreadWorker(String name, int shutdownWaitTime, ThreadFactory threadFactory) {
        this.name = name != null ? name : JavaUtils.getSimpleName(getClass());
        this.shutdownWaitTime = shutdownWaitTime;
        this.exec = threadFactory != null ? Executors.newSingleThreadExecutor(threadFactory)
                : Executors.newSingleThreadExecutor();
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

        try {
            ExecutorUtils.terminate(shutdownWaitTime, TimeUnit.MILLISECONDS, exec);
        } catch (Exception e) {
            log.error("Failed to stop {}", name, e);
        }

        if (log.isInfoEnabled()) {
            log.info("{} is stopped", name);
        }
    }

    @Override
    public void close() {
        stop();
    }

    public boolean isWorkerThread() {
        return this.workerThread == Thread.currentThread();
    }

    @Override
    public void run() {
        this.workerThread = Thread.currentThread();
        beforeMainLoop();
        while (this.state == STATE_START) {
            try {
                execute();
            } catch (InterruptedException ie) {
                // re-check state
            } catch (Throwable e) {
                log.error("{} throws exception", name, e);
            }
        }
        afterMainLoop();
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

    protected abstract void execute() throws InterruptedException;

}
