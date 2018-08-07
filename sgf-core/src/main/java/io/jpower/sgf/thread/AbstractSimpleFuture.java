package io.jpower.sgf.thread;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public abstract class AbstractSimpleFuture<V> implements SimpleFuture<V> {

    private static final Logger log = LoggerFactory.getLogger(AbstractSimpleFuture.class);

    private Object listeners;

    private boolean notifyingListeners;

    @Override
    public SimpleFuture<V> addListener(SimpleFutureListener<? extends SimpleFuture<? super V>> listener) {
        Objects.requireNonNull(listener, "listener");

        synchronized (this) {
            addListener0(listener);
        }

        if (isDone()) {
            notifyListeners();
        }

        return this;
    }

    @Override
    public SimpleFuture<V> addListeners(SimpleFutureListener<? extends SimpleFuture<? super V>>... listeners) {
        Objects.requireNonNull(listeners, "listeners");

        synchronized (this) {
            for (SimpleFutureListener<? extends SimpleFuture<? super V>> listener : listeners) {
                if (listener == null) {
                    break;
                }
                addListener0(listener);
            }
        }

        if (isDone()) {
            notifyListeners();
        }

        return this;
    }

    @Override
    public SimpleFuture<V> removeListener(SimpleFutureListener<? extends SimpleFuture<? super V>> listener) {
        Objects.requireNonNull(listener, "listener");

        synchronized (this) {
            removeListener0(listener);
        }

        return this;
    }

    @Override
    public SimpleFuture<V> removeListeners(SimpleFutureListener<? extends SimpleFuture<? super V>>... listeners) {
        Objects.requireNonNull(listeners, "listeners");

        synchronized (this) {
            for (SimpleFutureListener<? extends SimpleFuture<? super V>> listener : listeners) {
                if (listener == null) {
                    break;
                }
                removeListener0(listener);
            }
        }

        return this;
    }

    protected void notifyListeners() {
        Object listeners;
        synchronized (this) {
            if (notifyingListeners || this.listeners == null) {
                return;
            }
            notifyingListeners = true;
            listeners = this.listeners;
            this.listeners = null;
        }
        while (true) {
            if (listeners instanceof DefaultSimpleFutureListeners) {
                notifyListeners0((DefaultSimpleFutureListeners) listeners);
            } else {
                notifyListener0(this, (SimpleFutureListener<?>) listeners);
            }
            synchronized (this) {
                if (this.listeners == null) {
                    notifyingListeners = false;
                    return;
                }
                listeners = this.listeners;
                this.listeners = null;
            }
        }
    }

    private void addListener0(SimpleFutureListener<? extends SimpleFuture<? super V>> listener) {
        if (listeners == null) {
            listeners = listener;
        } else if (listeners instanceof DefaultSimpleFutureListeners) {
            ((DefaultSimpleFutureListeners) listeners).add(listener);
        } else {
            listeners = new DefaultSimpleFutureListeners((SimpleFutureListener<?>) listeners, listener);
        }
    }

    private void removeListener0(SimpleFutureListener<? extends SimpleFuture<? super V>> listener) {
        if (listeners instanceof DefaultSimpleFutureListeners) {
            ((DefaultSimpleFutureListeners) listeners).remove(listener);
        } else if (listeners == listener) {
            listeners = null;
        }
    }


    private void notifyListeners0(DefaultSimpleFutureListeners listeners) {
        SimpleFutureListener<?>[] a = listeners.listeners();
        int size = listeners.size();
        for (int i = 0; i < size; i++) {
            notifyListener0(this, a[i]);
        }
    }

    protected static void notifyListener(SimpleFuture<?> future, SimpleFutureListener<?> l) {
        notifyListener0(future, l);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void notifyListener0(SimpleFuture future, SimpleFutureListener l) {
        try {
            l.operationComplete(future);
        } catch (Throwable t) {
            if (log.isWarnEnabled()) {
                log.warn(
                        "An exception was thrown by " +
                                l.getClass().getSimpleName() + ".operationComplete()", t);
            }
        }
    }

}
