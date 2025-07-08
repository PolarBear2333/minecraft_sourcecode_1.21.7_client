/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.level;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkTaskPriorityQueue;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.PriorityConsecutiveExecutor;
import net.minecraft.util.thread.StrictQueue;
import net.minecraft.util.thread.TaskScheduler;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

public class ChunkTaskDispatcher
implements ChunkHolder.LevelChangeListener,
AutoCloseable {
    public static final int DISPATCHER_PRIORITY_COUNT = 4;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ChunkTaskPriorityQueue queue;
    private final TaskScheduler<Runnable> executor;
    private final PriorityConsecutiveExecutor dispatcher;
    protected boolean sleeping;

    public ChunkTaskDispatcher(TaskScheduler<Runnable> taskScheduler, Executor executor) {
        this.queue = new ChunkTaskPriorityQueue(taskScheduler.name() + "_queue");
        this.executor = taskScheduler;
        this.dispatcher = new PriorityConsecutiveExecutor(4, executor, "dispatcher");
        this.sleeping = true;
    }

    public boolean hasWork() {
        return this.dispatcher.hasWork() || this.queue.hasWork();
    }

    @Override
    public void onLevelChange(ChunkPos chunkPos, IntSupplier intSupplier, int n, IntConsumer intConsumer) {
        this.dispatcher.schedule(new StrictQueue.RunnableWithPriority(0, () -> {
            int n2 = intSupplier.getAsInt();
            this.queue.resortChunkTasks(n2, chunkPos, n);
            intConsumer.accept(n);
        }));
    }

    public void release(long l, Runnable runnable, boolean bl) {
        this.dispatcher.schedule(new StrictQueue.RunnableWithPriority(1, () -> {
            this.queue.release(l, bl);
            this.onRelease(l);
            if (this.sleeping) {
                this.sleeping = false;
                this.pollTask();
            }
            runnable.run();
        }));
    }

    public void submit(Runnable runnable, long l, IntSupplier intSupplier) {
        this.dispatcher.schedule(new StrictQueue.RunnableWithPriority(2, () -> {
            int n = intSupplier.getAsInt();
            this.queue.submit(runnable, l, n);
            if (this.sleeping) {
                this.sleeping = false;
                this.pollTask();
            }
        }));
    }

    protected void pollTask() {
        this.dispatcher.schedule(new StrictQueue.RunnableWithPriority(3, () -> {
            ChunkTaskPriorityQueue.TasksForChunk tasksForChunk = this.popTasks();
            if (tasksForChunk == null) {
                this.sleeping = true;
            } else {
                this.scheduleForExecution(tasksForChunk);
            }
        }));
    }

    protected void scheduleForExecution(ChunkTaskPriorityQueue.TasksForChunk tasksForChunk) {
        CompletableFuture.allOf((CompletableFuture[])tasksForChunk.tasks().stream().map(runnable -> this.executor.scheduleWithResult(completableFuture -> {
            runnable.run();
            completableFuture.complete(Unit.INSTANCE);
        })).toArray(CompletableFuture[]::new)).thenAccept(void_ -> this.pollTask());
    }

    protected void onRelease(long l) {
    }

    @Nullable
    protected ChunkTaskPriorityQueue.TasksForChunk popTasks() {
        return this.queue.pop();
    }

    @Override
    public void close() {
        this.executor.close();
    }
}

