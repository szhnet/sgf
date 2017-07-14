package io.jpower.sgf.thread;

import io.jpower.sgf.utils.JavaUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 修改自<code>org.jboss.netty.util.HashedWheelTimer</code>
 * <ul>
 * <li>支持<code>scheduleAtFixedRate</code>和<code>scheduleWithFixedDelay</code>
 * </li>
 * <li>改造了相关接口<code>SimpleFuture</code>, <code>TimerTask</code></li>
 * </ul>
 * <br>
 * <br>
 * <p>
 * A Timer optimized for approximated I/O timeout scheduling.
 * <p>
 * <h3>Tick Duration</h3>
 * <p>
 * As described with 'approximated', this timer does not execute the scheduled
 * {@link TimerTask} on time. {@link HashedWheelTimer}, on every tick, will
 * check if there are any {@link TimerTask}s behind the schedule and execute
 * them.
 * <p>
 * You can increase or decrease the accuracy of the execution timing by
 * specifying smaller or larger tick duration in the constructor. In most
 * network applications, I/O timeout does not need to be accurate. Therefore,
 * the default tick duration is 100 milliseconds and you will not need to try
 * different configurations in most cases.
 * <p>
 * <h3>Ticks per Wheel (Wheel Size)</h3>
 * <p>
 * {@link HashedWheelTimer} maintains a data structure called 'wheel'. To put
 * simply, a wheel is a hash table of {@link TimerTask}s whose hash function is
 * 'dead line of the task'. The default number of ticks per wheel (i.e. the size
 * of the wheel) is 512. You could specify a larger value if you are going to
 * schedule a lot of timeouts.
 * <p>
 * <h3>Do not create many instances.</h3>
 * <p>
 * {@link HashedWheelTimer} creates a new thread whenever it is instantiated and
 * started. Therefore, you should make sure to create only one instance and
 * share it across your application. One of the common mistakes, that makes your
 * application unresponsive, is to create a new instance in
 * ChannelPipelineFactory, which results in the creation of a new thread for
 * every connection.
 * <p>
 * <h3>Implementation Details</h3>
 * <p>
 * {@link HashedWheelTimer} is based on
 * <a href="http://cseweb.ucsd.edu/users/varghese/">George Varghese</a> and Tony
 * Lauck's paper,
 * <a href="http://cseweb.ucsd.edu/users/varghese/PAPERS/twheel.ps.Z">'Hashed
 * and Hierarchical Timing Wheels: data structures to efficiently implement a
 * timer facility'</a>. More comprehensive slides are located
 * <a href="http://www.cse.wustl.edu/~cdgill/courses/cs6874/TimingWheels.ppt">
 * here</a>.
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class HashedWheelTimer {

    private static final AtomicInteger id = new AtomicInteger();

    private static final AtomicIntegerFieldUpdater<HashedWheelTimer> WORKER_STATE_UPDATER = AtomicIntegerFieldUpdater
            .newUpdater(HashedWheelTimer.class, "workerState");

    private final Worker worker = new Worker();

    private final Thread workerThread;

    public static final int WORKER_STATE_INIT = 0;
    public static final int WORKER_STATE_STARTED = 1;
    public static final int WORKER_STATE_SHUTDOWN = 2;

    private volatile int workerState = WORKER_STATE_INIT;

    private final long tickDuration;

    private final HashedWheelBucket[] wheel;

    private final int mask;

    private final CountDownLatch startTimeInitialized = new CountDownLatch(1);

    private final Queue<TimerFutureTask> tasks = new ConcurrentLinkedQueue<TimerFutureTask>();

    /**
     * 以startTime为基础，求相对时间
     */
    private volatile long startTime;

    public HashedWheelTimer() {
        this(defaultThreadFactory());
    }

    public HashedWheelTimer(long tickDuration, TimeUnit unit) {
        this(defaultThreadFactory(), tickDuration, unit);
    }

    public HashedWheelTimer(long tickDuration, TimeUnit unit, int ticksPerWheel) {
        this(defaultThreadFactory(), tickDuration, unit, ticksPerWheel);
    }

    public HashedWheelTimer(ThreadFactory threadFactory) {
        this(threadFactory, 100, TimeUnit.MILLISECONDS);
    }

    public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit) {
        this(threadFactory, tickDuration, unit, 512);
    }

    public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit,
                            int ticksPerWheel) {
        if (threadFactory == null) {
            throw new NullPointerException("threadFactory");
        }
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        if (tickDuration <= 0) {
            throw new IllegalArgumentException(
                    "tickDuration must be greater than 0: " + tickDuration);
        }
        if (ticksPerWheel <= 0) {
            throw new IllegalArgumentException(
                    "ticksPerWheel must be greater than 0: " + ticksPerWheel);
        }

        // Normalize ticksPerWheel to power of two and initialize the wheel.
        wheel = createWheel(ticksPerWheel);
        mask = wheel.length - 1;

        // Convert tickDuration to nanos.
        this.tickDuration = unit.toNanos(tickDuration);

        // Prevent overflow.
        if (this.tickDuration >= Long.MAX_VALUE / wheel.length) {
            throw new IllegalArgumentException(
                    String.format("tickDuration: %d (expected: 0 < tickDuration in nanos < %d",
                            tickDuration, Long.MAX_VALUE / wheel.length));
        }

        workerThread = threadFactory.newThread(worker);
    }

    private HashedWheelBucket[] createWheel(int ticksPerWheel) {
        if (ticksPerWheel <= 0) {
            throw new IllegalArgumentException(
                    "ticksPerWheel must be greater than 0: " + ticksPerWheel);
        }
        if (ticksPerWheel > 1073741824) {
            throw new IllegalArgumentException(
                    "ticksPerWheel may not be greater than 2^30: " + ticksPerWheel);
        }

        ticksPerWheel = normalizeTicksPerWheel(ticksPerWheel);
        HashedWheelBucket[] wheel = new HashedWheelBucket[ticksPerWheel];
        for (int i = 0; i < wheel.length; i++) {
            wheel[i] = new HashedWheelBucket();
        }
        return wheel;
    }

    /**
     * 使ticksPerWheel为2的幂
     *
     * @param ticksPerWheel
     * @return
     */
    private int normalizeTicksPerWheel(int ticksPerWheel) {
        int normalizedTicksPerWheel = 1;
        while (normalizedTicksPerWheel < ticksPerWheel) {
            normalizedTicksPerWheel <<= 1;
        }
        return normalizedTicksPerWheel;
    }

    private static ThreadFactory defaultThreadFactory() {
        return new NamedThreadFactory("Hashed wheel timer #" + id.incrementAndGet());
    }

    public void start() {
        switch (WORKER_STATE_UPDATER.get(this)) {
            case WORKER_STATE_INIT:
                if (WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_INIT, WORKER_STATE_STARTED)) {
                    workerThread.start();
                }
                break;
            case WORKER_STATE_STARTED:
                break;
            case WORKER_STATE_SHUTDOWN:
                throw new IllegalStateException("cannot be started once stopped");
            default:
                throw new Error("Invalid WorkerState");
        }

        // Wait until the startTime is initialized by the worker.
        while (startTime == 0) { // 用0表示未初始化
            try {
                startTimeInitialized.await();
            } catch (InterruptedException ignored) {
                // Ignore - it will be ready very soon.
            }
        }
    }

    public Set<TimerTask> stop() {
        if (Thread.currentThread() == workerThread) {
            throw new IllegalStateException(HashedWheelTimer.class.getSimpleName()
                    + ".stop() cannot be called from " + TimerTask.class.getSimpleName());
        }

        if (!WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_STARTED,
                WORKER_STATE_SHUTDOWN)) {
            // workerState can be 0 or 2 at this moment - let it always be 2.
            WORKER_STATE_UPDATER.set(this, WORKER_STATE_SHUTDOWN);

            return Collections.emptySet();
        }

        boolean interrupted = false;
        while (workerThread.isAlive()) {
            workerThread.interrupt();
            try {
                workerThread.join(100);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }

        return worker.unprocessedTasks();
    }

    public Set<TimerTask> stop(long timeout, TimeUnit unit) {
        if (Thread.currentThread() == workerThread) {
            throw new IllegalStateException(HashedWheelTimer.class.getSimpleName()
                    + ".stop() cannot be called from " + TimerTask.class.getSimpleName());
        }

        if (!WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_STARTED,
                WORKER_STATE_SHUTDOWN)) {
            // workerState can be 0 or 2 at this moment - let it always be 2.
            WORKER_STATE_UPDATER.set(this, WORKER_STATE_SHUTDOWN);

            return Collections.emptySet();
        }

        boolean interrupted = false;
        long awaitTime = unit.toMillis(timeout);
        long lastTime = System.currentTimeMillis();
        while (awaitTime > 0 && workerThread.isAlive()) {
            workerThread.interrupt();
            try {
                workerThread.join(100);
            } catch (InterruptedException e) {
                interrupted = true;
            }
            long now = System.currentTimeMillis();
            awaitTime = awaitTime - (now - lastTime);
            lastTime = now;
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }

        if (workerThread.isAlive()) {
            return Collections.emptySet();
        } else {
            return worker.unprocessedTasks();
        }
    }

    public boolean isShutdown() {
        return workerState == WORKER_STATE_SHUTDOWN;
    }

    public SimpleFuture<Void> schedule(TimerTask task, long delay, TimeUnit unit) {
        if (task == null) {
            throw new NullPointerException("task");
        }
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        start();

        // Add the TimerFutureTask to the task queue which will be processed on
        // the next tick. During processing all the queued TimerFutureTask will
        // be added to the correct HashedWheelBucket.
        long deadline = triggerTime(delay, unit);
        TimerFutureTask futureTask = new TimerFutureTask(this, task, deadline);
        createdFuture(task, futureTask);
        tasks.add(futureTask);
        return futureTask;
    }

    public SimpleFuture<Void> scheduleAtFixedRate(TimerTask task, long initialDelay, long period,
                                                  TimeUnit unit) {
        if (task == null) {
            throw new NullPointerException("task");
        }
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        start();

        long deadline = triggerTime(initialDelay, unit);
        TimerFutureTask futureTask = new TimerFutureTask(this, task, deadline,
                unit.toNanos(period));
        createdFuture(task, futureTask);
        tasks.add(futureTask);
        return futureTask;
    }

    public SimpleFuture<Void> scheduleWithFixedDelay(TimerTask task, long initialDelay, long period,
                                                     TimeUnit unit) {
        if (task == null) {
            throw new NullPointerException("task");
        }
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        start();

        long deadline = triggerTime(initialDelay, unit);
        TimerFutureTask futureTask = new TimerFutureTask(this, task, deadline,
                unit.toNanos(-period));
        createdFuture(task, futureTask);
        tasks.add(futureTask);
        return futureTask;
    }

    private long now() {
        return System.nanoTime() - startTime;
    }

    private long triggerTime(long delay, TimeUnit unit) {
        return unit.toNanos(delay) + now();
    }

    protected void createdFuture(TimerTask task, SimpleFuture<Void> future) {

    }

    private final class Worker implements Runnable {

        private final Set<TimerTask> unprocessedTasks = new HashSet<TimerTask>();

        private long tick;

        @Override
        public void run() {
            // Initialize the startTime.
            startTime = System.nanoTime();
            if (startTime == 0) {
                // We use 0 as an indicator for the uninitialized value here, so
                // make sure it's not 0 when initialized.
                startTime = 1;
            }

            // Notify the other threads waiting for the initialization at
            // start().
            startTimeInitialized.countDown();

            do {
                final long deadline = waitForNextTick();
                if (deadline > 0) {
                    transferTasksToBuckets();
                    HashedWheelBucket bucket = wheel[(int) (tick & mask)];
                    bucket.expireTasks(HashedWheelTimer.this, deadline);
                    tick++;
                }
            } while (WORKER_STATE_UPDATER.get(HashedWheelTimer.this) == WORKER_STATE_STARTED);

            // Fill the unprocessedTasks so we can return them from stop() method.
            for (HashedWheelBucket bucket : wheel) {
                bucket.clearTasks(unprocessedTasks);
            }
            for (; ; ) {
                TimerFutureTask futureTask = tasks.poll();
                if (futureTask == null) {
                    break;
                }
                if (!futureTask.isCancelled()) {
                    unprocessedTasks.add(futureTask.sync.task);
                }
            }
        }

        /**
         * calculate goal nanoTime from startTime and current tick number, then
         * wait until that goal has been reached.
         *
         * @return Long.MIN_VALUE if received a shutdown request, current time
         * otherwise (with Long.MIN_VALUE changed by +1)
         */
        private long waitForNextTick() {
            long deadline = tickDuration * (tick + 1);

            for (; ; ) {
                final long currentTime = now();
                long sleepTimeMs = (deadline - currentTime + 999999) / 1000000; // 保证sleep的粒度最少为1ms

                if (sleepTimeMs <= 0) {
                    if (currentTime == Long.MIN_VALUE) {
                        return -Long.MAX_VALUE;
                    } else {
                        return currentTime;
                    }
                }

                // Check if we run on windows, as if thats the case we will need
                // to round the sleepTime as workaround for a bug that only
                // affect
                // the JVM if it runs on windows.
                //
                // See https://github.com/netty/netty/issues/356
                if (JavaUtils.isWindows()) {
                    sleepTimeMs = sleepTimeMs / 10 * 10;
                }

                try {
                    Thread.sleep(sleepTimeMs);
                } catch (InterruptedException e) {
                    if (WORKER_STATE_UPDATER.get(HashedWheelTimer.this) == WORKER_STATE_SHUTDOWN) {
                        return Long.MIN_VALUE;
                    }
                }
            }
        }

        private void transferTasksToBuckets() {
            // transfer only max. 100000 tasks per tick to prevent a thread
            // to stale the workerThread when it just
            // adds new tasks in a loop.
            for (int i = 0; i < 100000; i++) {
                TimerFutureTask futureTask = tasks.poll();
                if (futureTask == null) {
                    // all processed
                    break;
                }
                if (futureTask.isCancelled() || !futureTask.inBucket()) {
                    // Was cancelled in the meantime. So just remove it and
                    // continue with next TimerFutureTask
                    // in the queue
                    futureTask.remove();
                    continue;
                }
                long calculated = futureTask.deadline / tickDuration;
                long remainingRounds = (calculated - tick) / wheel.length;
                futureTask.remainingRounds = remainingRounds;

                final long ticks = Math.max(calculated, tick); // Ensure we
                // don't schedule
                // for past.
                int stopIndex = (int) (ticks & mask);

                HashedWheelBucket bucket = wheel[stopIndex];
                bucket.add(futureTask);
            }
        }

        public Set<TimerTask> unprocessedTasks() {
            return Collections.unmodifiableSet(unprocessedTasks);
        }

    }

    private static class TimerFutureTask extends AbstractSimpleFuture<Void> {

        private final HashedWheelTimer timer;

        private final Sync sync;

        private long deadline;

        /**
         * Period in nanoseconds for repeating tasks. A positive value indicates
         * fixed-rate execution. A negative value indicates fixed-delay
         * execution. A value of 0 indicates a non-repeating task.
         */
        private final long period;

        // remainingRounds will be calculated and set by
        // Worker.transferTasksToBuckets() before the
        // TimerFutureTask will be added to the correct HashedWheelBucket.
        long remainingRounds;

        // This will be used to chain tasks in HashedWheelTimerBucket via a
        // double-linked-list.
        // As only the workerThread will act on it there is no need for
        // synchronization / volatile.
        TimerFutureTask next;
        TimerFutureTask prev;

        // The bucket to which the task was added
        HashedWheelBucket bucket;

        TimerFutureTask(HashedWheelTimer timer, TimerTask task, long deadline) {
            this.timer = timer;
            this.sync = new Sync(task);
            this.deadline = deadline;
            this.period = 0;
        }

        TimerFutureTask(HashedWheelTimer timer, TimerTask task, long deadline, long period) {
            this.timer = timer;
            this.sync = new Sync(task);
            this.deadline = deadline;
            this.period = period;
        }

        @Override
        public boolean isSuccess() {
            return sync.innerIsSuccess();
        }

        @Override
        public SimpleFuture<Void> setSuccess(Void result) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean trySuccess(Void result) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Throwable getCause() {
            return sync.innerGetThrowable();
        }

        @Override
        public SimpleFuture<Void> setFailure(Throwable cause) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean tryFailure(Throwable cause) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isCancelled() {
            return sync.innerIsCancelled();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return sync.innerCancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isDone() {
            return sync.innerIsDone();
        }

        public boolean isInBucket() {
            return sync.innerIsInBucket();
        }

        public boolean inBucket() {
            return sync.innerInBucket();
        }

        @Override
        public Void tryGet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Void get() throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Void getUninterruptibly() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Void get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Void get(long timeoutMillis) throws InterruptedException, TimeoutException {
            throw new UnsupportedOperationException();
        }

        @Override
        public SimpleFuture<Void> await() throws InterruptedException {
            sync.acquireSharedInterruptibly(0);
            return this;
        }

        @Override
        public SimpleFuture<Void> awaitUninterruptibly() {
            sync.acquireShared(0);
            return this;
        }

        @Override
        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return sync.tryAcquireSharedNanos(0, unit.toNanos(timeout));
        }

        @Override
        public boolean await(long timeoutMillis) throws InterruptedException {
            return sync.tryAcquireSharedNanos(0, TimeUnit.MILLISECONDS.toNanos(timeoutMillis));
        }

        void run() {
            if (isPeriodic()) {
                runPeriodic();
            } else {
                sync.innerRun();
            }
        }

        /**
         * Returns true if this is a periodic (not a one-shot) action.
         *
         * @return true if periodic
         */
        boolean isPeriodic() {
            return period != 0;
        }

        /**
         * Runs a periodic task.
         */
        void runPeriodic() {
            boolean ok = sync.innerRunAndReset();
            boolean down = timer.isShutdown();
            // Reschedule if not cancelled and not shutdown or policy allows
            if (ok && !down) {
                long p = period;
                if (p > 0) {
                    deadline += p;
                } else {
                    deadline = timer.triggerTime(-p, TimeUnit.NANOSECONDS);
                }
                timer.tasks.add(this);
            }
        }

        void remove() {
            if (bucket != null) {
                bucket.remove(this);
            }
        }

        private final class Sync extends AbstractQueuedSynchronizer {

            private static final long serialVersionUID = 8138953041726108575L;

            private static final int INIT = 0;

            private static final int IN_BUCKET = 1;

            private static final int RUNNING = 2;

            private static final int RAN = 3;

            private static final int CANCELLED = 4;

            private final TimerTask task;

            /**
             * The exception to throw from get()
             */
            private Throwable exception;

            private volatile Thread runner;

            Sync(TimerTask task) {
                this.task = task;
            }

            private boolean ranOrCancelled(int state) {
                return state == RAN || state == CANCELLED;
            }

            @Override
            protected int tryAcquireShared(int ignore) {
                return innerIsDone() ? 1 : -1;
            }

            @Override
            protected boolean tryReleaseShared(int ignore) {
                runner = null;
                return true;
            }

            boolean innerIsCancelled() {
                return getState() == CANCELLED;
            }

            boolean innerIsDone() {
                return ranOrCancelled(getState()) && runner == null;
            }

            boolean innerIsSuccess() {
                return ranOrCancelled(getState()) && runner == null && exception == null;
            }

            boolean innerIsInBucket() {
                return getState() == IN_BUCKET;
            }

            boolean innerInBucket() {
                return compareAndSetState(INIT, IN_BUCKET);
            }

            Throwable innerGetThrowable() {
                return exception;
            }

            void innerSet() {
                for (; ; ) {
                    int s = getState();
                    if (s == RAN) {
                        return;
                    }
                    if (s == CANCELLED) {
                        // aggressively release to set runner to null,
                        // in case we are racing with a cancel request
                        // that will try to interrupt runner
                        releaseShared(0); // szh:
                        // 这里可能和cancel的请求有冲突，cannel方法可能会interrupt
                        // runner线程，而innerSet和innerSetException方法都是runner线程调用的，这里调用releaseShared将runner置null，使得runner线程尽量不被interrupt，因为此时已经设置结果，runner已经执行完毕，runner线程无需interrupt。
                        return;
                    }
                    if (compareAndSetState(s, RAN)) {
                        releaseShared(0);
                        return;
                    }
                }
            }

            void innerSetException(Throwable t) {
                for (; ; ) {
                    int s = getState();
                    if (s == RAN) {
                        return;
                    }
                    if (s == CANCELLED) {
                        // aggressively release to set runner to null,
                        // in case we are racing with a cancel request
                        // that will try to interrupt runner
                        releaseShared(0);
                        return;
                    }
                    if (compareAndSetState(s, RAN)) {
                        exception = t;
                        releaseShared(0);
                        return;
                    }
                }
            }

            boolean innerCancel(boolean mayInterruptIfRunning) {
                int s;
                for (; ; ) {
                    s = getState();
                    if (ranOrCancelled(s)) {
                        return false;
                    }
                    if (compareAndSetState(s, CANCELLED)) {
                        break;
                    }
                }
                if (mayInterruptIfRunning) {
                    Thread r = runner;
                    if (r != null) {
                        r.interrupt();
                    }
                }
                releaseShared(0);
                if (s == IN_BUCKET) {
                    timer.tasks.add(TimerFutureTask.this); // 将此对象放回任务队列，以便在下次tick的时候就进行cancel处理（见transferTasksToBuckets方法）。
                }
                return true;
            }

            void innerRun() {
                if (!compareAndSetState(IN_BUCKET, RUNNING)) {
                    return;
                }
                try {
                    runner = Thread.currentThread();
                    if (getState() == RUNNING) { // recheck after setting thread
                        task.run(TimerFutureTask.this);
                        innerSet();
                    } else {
                        releaseShared(0); // cancel
                    }
                } catch (Throwable ex) {
                    innerSetException(ex);
                }
            }

            boolean innerRunAndReset() {
                if (!compareAndSetState(IN_BUCKET, RUNNING)) {
                    return false;
                }
                try {
                    runner = Thread.currentThread();
                    if (getState() == RUNNING) {
                        task.run(TimerFutureTask.this); // don't set result
                    }
                    runner = null;
                    if (!compareAndSetState(RUNNING, INIT)) {
                        return false;
                    }
                    return true;
                } catch (Throwable ex) {
                    innerSetException(ex);
                    return false;
                }
            }

        }

    }

    /**
     * Bucket that stores TimerFutureTasks. These are stored in a linked-list
     * like datastructure to allow easy removal of TimerFutureTasks in the
     * middle. Also the TimerFutureTask act as nodes themself and so no extra
     * object creation is needed.
     */
    private static final class HashedWheelBucket {

        // Used for the linked-list datastructure
        private TimerFutureTask head;
        private TimerFutureTask tail;

        /**
         * Add {@link TimerFutureTask} to this bucket.
         */
        void add(TimerFutureTask futureTask) {
            assert futureTask.bucket == null;
            futureTask.bucket = this;
            if (head == null) {
                head = tail = futureTask;
            } else {
                tail.next = futureTask;
                futureTask.prev = tail;
                tail = futureTask;
            }
        }

        /**
         * Expire all {@link TimerFutureTask}s for the given {@code deadline}.
         */
        public void expireTasks(HashedWheelTimer timer, long deadline) {
            TimerFutureTask futureTask = head;

            // process all timeouts
            while (futureTask != null) {
                boolean remove = false;
                if (futureTask.remainingRounds <= 0) {
                    if (futureTask.deadline <= deadline) {
                        runTask(timer, futureTask);
                    } else {
                        // The timeout was placed into a wrong slot. This should
                        // never happen.
                        throw new IllegalStateException(
                                String.format("timeout.deadline (%d) > deadline (%d)",
                                        futureTask.deadline, deadline));
                    }
                    remove = true;
                } else if (futureTask.isCancelled()) {
                    remove = true;
                } else {
                    futureTask.remainingRounds--;
                }
                // store reference to next as we may null out
                // TimerFutureTask.next in
                // the remove block.
                TimerFutureTask next = futureTask.next;
                if (remove) {
                    remove(futureTask);
                }
                futureTask = next;
            }
        }

        private void runTask(HashedWheelTimer timer, TimerFutureTask futureTask) {
            if ((timer.isShutdown() || (Thread.interrupted() && timer.isShutdown()))
                    && !timer.workerThread.isInterrupted()) {
                timer.workerThread.interrupt();
            }

            futureTask.run();
        }

        public void remove(TimerFutureTask futureTask) {
            TimerFutureTask prev = futureTask.prev;
            TimerFutureTask next = futureTask.next;

            if (prev == null) {
                head = next;
            } else {
                prev.next = next;
                futureTask.prev = null;
            }

            if (next == null) {
                tail = prev;
            } else {
                next.prev = prev;
                futureTask.next = null;
            }

            futureTask.bucket = null;
        }

        /**
         * Clear this bucket and return all IN_BUCKET {@link TimerFutureTask}s.
         */
        public void clearTasks(Set<TimerTask> set) {
            for (; ; ) {
                TimerFutureTask futureTask = pollTask();
                if (futureTask == null) {
                    return;
                }
                if (futureTask.isInBucket()) {
                    set.add(futureTask.sync.task);
                }
            }
        }

        private TimerFutureTask pollTask() {
            TimerFutureTask head = this.head;
            if (head == null) {
                return null;
            }
            TimerFutureTask next = head.next;
            if (next == null) {
                tail = this.head = null;
            } else {
                this.head = next;
                next.prev = null;
            }

            // null out prev and next to allow for GC.
            head.next = null;
            head.prev = null;
            return head;
        }

    }

}
