/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Queues
 *  javax.annotation.Nullable
 */
package net.minecraft.util.thread;

import com.google.common.collect.Queues;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;

public interface StrictQueue<T extends Runnable> {
    @Nullable
    public Runnable pop();

    public boolean push(T var1);

    public boolean isEmpty();

    public int size();

    public static final class FixedPriorityQueue
    implements StrictQueue<RunnableWithPriority> {
        private final Queue<Runnable>[] queues;
        private final AtomicInteger size = new AtomicInteger();

        public FixedPriorityQueue(int n) {
            this.queues = new Queue[n];
            for (int i = 0; i < n; ++i) {
                this.queues[i] = Queues.newConcurrentLinkedQueue();
            }
        }

        @Override
        @Nullable
        public Runnable pop() {
            for (Queue<Runnable> queue : this.queues) {
                Runnable runnable = queue.poll();
                if (runnable == null) continue;
                this.size.decrementAndGet();
                return runnable;
            }
            return null;
        }

        @Override
        public boolean push(RunnableWithPriority runnableWithPriority) {
            int n = runnableWithPriority.priority;
            if (n >= this.queues.length || n < 0) {
                throw new IndexOutOfBoundsException(String.format(Locale.ROOT, "Priority %d not supported. Expected range [0-%d]", n, this.queues.length - 1));
            }
            this.queues[n].add(runnableWithPriority);
            this.size.incrementAndGet();
            return true;
        }

        @Override
        public boolean isEmpty() {
            return this.size.get() == 0;
        }

        @Override
        public int size() {
            return this.size.get();
        }
    }

    public static final class RunnableWithPriority
    extends Record
    implements Runnable {
        final int priority;
        private final Runnable task;

        public RunnableWithPriority(int n, Runnable runnable) {
            this.priority = n;
            this.task = runnable;
        }

        @Override
        public void run() {
            this.task.run();
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RunnableWithPriority.class, "priority;task", "priority", "task"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RunnableWithPriority.class, "priority;task", "priority", "task"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RunnableWithPriority.class, "priority;task", "priority", "task"}, this, object);
        }

        public int priority() {
            return this.priority;
        }

        public Runnable task() {
            return this.task;
        }
    }

    public static final class QueueStrictQueue
    implements StrictQueue<Runnable> {
        private final Queue<Runnable> queue;

        public QueueStrictQueue(Queue<Runnable> queue) {
            this.queue = queue;
        }

        @Override
        @Nullable
        public Runnable pop() {
            return this.queue.poll();
        }

        @Override
        public boolean push(Runnable runnable) {
            return this.queue.add(runnable);
        }

        @Override
        public boolean isEmpty() {
            return this.queue.isEmpty();
        }

        @Override
        public int size() {
            return this.queue.size();
        }
    }
}

