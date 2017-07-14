package io.jpower.sgf.thread;

import java.util.Arrays;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
final class DefaultSimpleFutureListeners {

    private SimpleFutureListener<? extends SimpleFuture<?>>[] listeners;

    private int size;

    DefaultSimpleFutureListeners(SimpleFutureListener<? extends SimpleFuture<?>> first, SimpleFutureListener<? extends SimpleFuture<?>> second) {
        listeners = new SimpleFutureListener[2];
        listeners[0] = first;
        listeners[1] = second;
        size = 2;
    }

    public void add(SimpleFutureListener<? extends SimpleFuture<?>> l) {
        SimpleFutureListener<? extends SimpleFuture<?>>[] listeners = this.listeners;
        final int size = this.size;
        if (size == listeners.length) {
            this.listeners = listeners = Arrays.copyOf(listeners, size << 1);
        }
        listeners[size] = l;
        this.size = size + 1;
    }

    public void remove(SimpleFutureListener<? extends SimpleFuture<?>> l) {
        final SimpleFutureListener<? extends SimpleFuture<?>>[] listeners = this.listeners;
        int size = this.size;
        for (int i = 0; i < size; i++) {
            if (listeners[i] == l) {
                int listenersToMove = size - i - 1;
                if (listenersToMove > 0) {
                    System.arraycopy(listeners, i + 1, listeners, i, listenersToMove);
                }
                listeners[--size] = null;
                this.size = size;

                return;
            }
        }
    }

    public SimpleFutureListener<? extends SimpleFuture<?>>[] listeners() {
        return listeners;
    }

    public int size() {
        return size;
    }

}
