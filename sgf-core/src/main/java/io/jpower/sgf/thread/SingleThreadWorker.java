package io.jpower.sgf.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jpower.sgf.utils.JavaUtils;

/**
 * @author zheng.sun
 */
public abstract class SingleThreadWorker implements Runnable {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final int STATE_INIT = 0;

    private static final int STATE_START = 1;

    private static final int STATE_STOP = 2;

    private ExecutorService exec = Executors.newSingleThreadExecutor();

    private volatile int state = STATE_INIT;

    private String name;

    private int shutdownWaitTime = 5000;

    private volatile Thread workerThread;

    protected SingleThreadWorker() {
        this.name = JavaUtils.getSimpleName(getClass());
    }

    protected SingleThreadWorker(String name) {
        this.name = name;
    }

    protected SingleThreadWorker(int shutdownWaitTime) {
        this.name = JavaUtils.getSimpleName(getClass());
        this.shutdownWaitTime = shutdownWaitTime;
    }

    protected SingleThreadWorker(String name, int shutdownWaitTime) {
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
            log.info(name + " is started");
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
            log.info(name + " is stopped");
        }
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
                log.error(name + " throws exception", e);
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
