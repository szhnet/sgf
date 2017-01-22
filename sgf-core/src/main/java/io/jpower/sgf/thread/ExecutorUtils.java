package io.jpower.sgf.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zheng.sun
 */
public class ExecutorUtils {

    private ExecutorUtils() {
    }

    public static void terminate(int awaitTime, TimeUnit timeunit, ExecutorService exec) {
        terminate(awaitTime, timeunit, true, exec);
    }

    public static void terminate(int awaitTime, TimeUnit timeunit, boolean shutdownNow,
                                 ExecutorService exec) {
        if (shutdownNow) {
            exec.shutdownNow();
        } else {
            exec.shutdown();
        }

        long awaitNano = timeunit.toNanos(awaitTime);
        boolean interrupted = false;
        boolean terminated = false;
        long lastTime = System.nanoTime();
        while (awaitNano > 0 && !terminated) {
            try {
                if (exec.awaitTermination(awaitTime, timeunit)) {
                    terminated = true;
                }
            } catch (InterruptedException e) {
                interrupted = true;
            }
            long now = System.nanoTime();
            awaitNano = awaitNano - (now - lastTime);
            lastTime = now;
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    public static void terminate(int awaitTime, TimeUnit timeunit, ExecutorService... execs) {
        terminate(awaitTime, timeunit, true, execs);
    }

    public static void terminate(int awaitTime, TimeUnit timeunit, boolean shutdownNow,
                                 ExecutorService... execs) {
        for (ExecutorService exec : execs) {
            if (shutdownNow) {
                exec.shutdownNow();
            } else {
                exec.shutdown();
            }
        }

        long awaitNano = timeunit.toNanos(awaitTime);
        long lastTime = System.nanoTime();
        boolean interrupted = false;
        for (ExecutorService exec : execs) {
            boolean terminated = false;
            while (awaitNano > 0 && !terminated) {
                try {
                    if (exec.awaitTermination(awaitTime, timeunit)) {
                        terminated = true;
                    }
                } catch (InterruptedException e) {
                    interrupted = true;
                }
                long now = System.nanoTime();
                awaitNano = awaitNano - (now - lastTime);
                lastTime = now;
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

}
