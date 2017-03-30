package io.jpower.sgf.thread;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public interface TimerTask {

    void run(SimpleFuture<Void> task);

}
