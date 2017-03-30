package io.jpower.sgf.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 一个Runnable的包装器。执行时将线程的名字改为指定的名字，执行结束后再恢复。
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class NamedRunable implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(NamedRunable.class);

    private final String newName;

    private final Runnable runnable;

    public NamedRunable(String newName, Runnable runnable) {
        if (newName == null) {
            throw new NullPointerException("newName");
        }
        if (runnable == null) {
            throw new NullPointerException("runnable");
        }

        this.newName = newName;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        final Thread currentThread = Thread.currentThread();
        final String oldThreadName = currentThread.getName();

        boolean renamed = false;
        if (!oldThreadName.equals(newName)) {
            try {
                currentThread.setName(newName);
                renamed = true;
            } catch (SecurityException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Failed to rename a thread due to security restriction.", e);
                }
            }
        }

        try {
            runnable.run();
        } finally {
            if (renamed) {
                // Revert the name back if the current thread was renamed.
                // We do not check the exception here because we know it works.
                currentThread.setName(oldThreadName);
            }
        }
    }

}
