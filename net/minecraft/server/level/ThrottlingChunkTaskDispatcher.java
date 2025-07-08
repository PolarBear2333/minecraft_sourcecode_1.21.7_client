/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  javax.annotation.Nullable
 */
package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.server.level.ChunkTaskDispatcher;
import net.minecraft.server.level.ChunkTaskPriorityQueue;
import net.minecraft.util.thread.TaskScheduler;
import net.minecraft.world.level.ChunkPos;

public class ThrottlingChunkTaskDispatcher
extends ChunkTaskDispatcher {
    private final LongSet chunkPositionsInExecution = new LongOpenHashSet();
    private final int maxChunksInExecution;
    private final String executorSchedulerName;

    public ThrottlingChunkTaskDispatcher(TaskScheduler<Runnable> taskScheduler, Executor executor, int n) {
        super(taskScheduler, executor);
        this.maxChunksInExecution = n;
        this.executorSchedulerName = taskScheduler.name();
    }

    @Override
    protected void onRelease(long l) {
        this.chunkPositionsInExecution.remove(l);
    }

    @Override
    @Nullable
    protected ChunkTaskPriorityQueue.TasksForChunk popTasks() {
        return this.chunkPositionsInExecution.size() < this.maxChunksInExecution ? super.popTasks() : null;
    }

    @Override
    protected void scheduleForExecution(ChunkTaskPriorityQueue.TasksForChunk tasksForChunk) {
        this.chunkPositionsInExecution.add(tasksForChunk.chunkPos());
        super.scheduleForExecution(tasksForChunk);
    }

    @VisibleForTesting
    public String getDebugStatus() {
        return this.executorSchedulerName + "=[" + this.chunkPositionsInExecution.longStream().mapToObj(l -> l + ":" + String.valueOf(new ChunkPos(l))).collect(Collectors.joining(",")) + "], s=" + this.sleeping;
    }
}

