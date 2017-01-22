package io.jpower.sgf.thread;

/**
 * @author zheng.sun
 */
public interface TimerTask {

    void run(SimpleFuture<Void> task);

}
