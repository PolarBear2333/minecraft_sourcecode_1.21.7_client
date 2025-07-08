/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.util.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.Mth;

public class ParallelMapTransform {
    private static final int DEFAULT_TASKS_PER_THREAD = 16;

    public static <K, U, V> CompletableFuture<Map<K, V>> schedule(Map<K, U> map, BiFunction<K, U, V> biFunction, int n, Executor executor) {
        int n2 = map.size();
        if (n2 == 0) {
            return CompletableFuture.completedFuture(Map.of());
        }
        if (n2 == 1) {
            Map.Entry<K, U> entry = map.entrySet().iterator().next();
            Object k = entry.getKey();
            Object u = entry.getValue();
            return CompletableFuture.supplyAsync(() -> {
                Object r = biFunction.apply(k, u);
                return r != null ? Map.of(k, r) : Map.of();
            }, executor);
        }
        SplitterBase splitterBase = n2 <= n ? new SingleTaskSplitter<K, U, V>(biFunction, n2) : new BatchedTaskSplitter<K, U, V>(biFunction, n2, n);
        return splitterBase.scheduleTasks(map, executor);
    }

    public static <K, U, V> CompletableFuture<Map<K, V>> schedule(Map<K, U> map, BiFunction<K, U, V> biFunction, Executor executor) {
        int n = Util.maxAllowedExecutorThreads() * 16;
        return ParallelMapTransform.schedule(map, biFunction, n, executor);
    }

    static class SingleTaskSplitter<K, U, V>
    extends SplitterBase<K, U, V> {
        SingleTaskSplitter(BiFunction<K, U, V> biFunction, int n) {
            super(biFunction, n, n);
        }

        @Override
        protected int batchSize(int n) {
            return 1;
        }

        @Override
        protected CompletableFuture<?> scheduleBatch(Container<K, U, V> container, int n, int n2, Executor executor) {
            assert (n + 1 == n2);
            return CompletableFuture.runAsync(() -> container.applyOperation(n), executor);
        }

        @Override
        protected CompletableFuture<Map<K, V>> scheduleFinalOperation(CompletableFuture<?> completableFuture, Container<K, U, V> container) {
            return completableFuture.thenApply(object -> {
                HashMap hashMap = new HashMap(container.size());
                for (int i = 0; i < container.size(); ++i) {
                    container.copyOut(i, hashMap);
                }
                return hashMap;
            });
        }
    }

    static class BatchedTaskSplitter<K, U, V>
    extends SplitterBase<K, U, V> {
        private final Map<K, V> result;
        private final int batchSize;
        private final int firstUndersizedBatchIndex;

        BatchedTaskSplitter(BiFunction<K, U, V> biFunction, int n, int n2) {
            super(biFunction, n, n2);
            this.result = new HashMap(n);
            this.batchSize = Mth.positiveCeilDiv(n, n2);
            int n3 = this.batchSize * n2;
            int n4 = n3 - n;
            this.firstUndersizedBatchIndex = n2 - n4;
            assert (this.firstUndersizedBatchIndex > 0 && this.firstUndersizedBatchIndex <= n2);
        }

        @Override
        protected CompletableFuture<?> scheduleBatch(Container<K, U, V> container, int n, int n2, Executor executor) {
            int n3 = n2 - n;
            assert (n3 == this.batchSize || n3 == this.batchSize - 1);
            return CompletableFuture.runAsync(BatchedTaskSplitter.createTask(this.result, n, n2, container), executor);
        }

        @Override
        protected int batchSize(int n) {
            return n < this.firstUndersizedBatchIndex ? this.batchSize : this.batchSize - 1;
        }

        private static <K, U, V> Runnable createTask(Map<K, V> map, int n, int n2, Container<K, U, V> container) {
            return () -> {
                for (int i = n; i < n2; ++i) {
                    container.applyOperation(i);
                }
                Map map2 = map;
                synchronized (map2) {
                    for (int i = n; i < n2; ++i) {
                        container.copyOut(i, map);
                    }
                }
            };
        }

        @Override
        protected CompletableFuture<Map<K, V>> scheduleFinalOperation(CompletableFuture<?> completableFuture, Container<K, U, V> container) {
            Map map = this.result;
            return completableFuture.thenApply(object -> map);
        }
    }

    static abstract class SplitterBase<K, U, V> {
        private int lastScheduledIndex;
        private int currentIndex;
        private final CompletableFuture<?>[] tasks;
        private int batchIndex;
        private final Container<K, U, V> container;

        SplitterBase(BiFunction<K, U, V> biFunction, int n, int n2) {
            this.container = new Container<K, U, V>(biFunction, n);
            this.tasks = new CompletableFuture[n2];
        }

        private int pendingBatchSize() {
            return this.currentIndex - this.lastScheduledIndex;
        }

        public CompletableFuture<Map<K, V>> scheduleTasks(Map<K, U> map, Executor executor) {
            map.forEach((object, object2) -> {
                this.container.put(this.currentIndex++, object, object2);
                if (this.pendingBatchSize() == this.batchSize(this.batchIndex)) {
                    this.tasks[this.batchIndex++] = this.scheduleBatch(this.container, this.lastScheduledIndex, this.currentIndex, executor);
                    this.lastScheduledIndex = this.currentIndex;
                }
            });
            assert (this.currentIndex == this.container.size());
            assert (this.lastScheduledIndex == this.currentIndex);
            assert (this.batchIndex == this.tasks.length);
            return this.scheduleFinalOperation(CompletableFuture.allOf(this.tasks), this.container);
        }

        protected abstract int batchSize(int var1);

        protected abstract CompletableFuture<?> scheduleBatch(Container<K, U, V> var1, int var2, int var3, Executor var4);

        protected abstract CompletableFuture<Map<K, V>> scheduleFinalOperation(CompletableFuture<?> var1, Container<K, U, V> var2);
    }

    record Container<K, U, V>(BiFunction<K, U, V> operation, Object[] keys, Object[] values) {
        public Container(BiFunction<K, U, V> biFunction, int n) {
            this(biFunction, new Object[n], new Object[n]);
        }

        public void put(int n, K k, U u) {
            this.keys[n] = k;
            this.values[n] = u;
        }

        @Nullable
        private K key(int n) {
            return (K)this.keys[n];
        }

        @Nullable
        private V output(int n) {
            return (V)this.values[n];
        }

        @Nullable
        private U input(int n) {
            return (U)this.values[n];
        }

        public void applyOperation(int n) {
            this.values[n] = this.operation.apply(this.key(n), this.input(n));
        }

        public void copyOut(int n, Map<K, V> map) {
            V v = this.output(n);
            if (v != null) {
                K k = this.key(n);
                map.put(k, v);
            }
        }

        public int size() {
            return this.keys.length;
        }
    }
}

