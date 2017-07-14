package io.jpower.sgf.thread;

import java.util.EventListener;

/**
 * Listens to the result of {@link SimpleFuture}.
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public interface SimpleFutureListener<F extends SimpleFuture<?>> extends EventListener {

    /**
     * Invoked when the operation associated with the {@link SimpleFuture} has been completed.
     *
     * @param future the source {@link SimpleFuture} which called this callback
     * @throws Exception
     */
    void operationComplete(F future) throws Exception;

}
