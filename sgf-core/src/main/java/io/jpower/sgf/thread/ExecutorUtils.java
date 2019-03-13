package io.jpower.sgf.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class ExecutorUtils {

    private ExecutorUtils() {
    }

    public static boolean terminate(int awaitTime, TimeUnit timeunit, ExecutorService exec) {
        return terminate(awaitTime, timeunit, true, exec);
    }

    public static boolean terminate(int awaitTime, TimeUnit timeunit, boolean shutdownNow,
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

        return terminated;
    }

    public static boolean terminate(int awaitTime, TimeUnit timeunit, ExecutorService... execs) {
        return terminate(awaitTime, timeunit, true, execs);
    }

    public static boolean terminate(int awaitTime, TimeUnit timeunit, boolean shutdownNow,
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
        boolean allTerminated = true;
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
            if (!terminated) {
                allTerminated = false;
                break; // 这种情况awaitNano应该已经<=0了
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }

        return allTerminated;
    }

}
