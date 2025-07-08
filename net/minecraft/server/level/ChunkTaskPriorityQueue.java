/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.server.level;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.world.level.ChunkPos;

public class ChunkTaskPriorityQueue {
    public static final int PRIORITY_LEVEL_COUNT = ChunkLevel.MAX_LEVEL + 2;
    private final List<Long2ObjectLinkedOpenHashMap<List<Runnable>>> queuesPerPriority = IntStream.range(0, PRIORITY_LEVEL_COUNT).mapToObj(n -> new Long2ObjectLinkedOpenHashMap()).toList();
    private volatile int topPriorityQueueIndex = PRIORITY_LEVEL_COUNT;
    private final String name;

    public ChunkTaskPriorityQueue(String string) {
        this.name = string;
    }

    protected void resortChunkTasks(int n, ChunkPos chunkPos, int n2) {
        if (n >= PRIORITY_LEVEL_COUNT) {
            return;
        }
        Long2ObjectLinkedOpenHashMap<List<Runnable>> long2ObjectLinkedOpenHashMap = this.queuesPerPriority.get(n);
        List list = (List)long2ObjectLinkedOpenHashMap.remove(chunkPos.toLong());
        if (n == this.topPriorityQueueIndex) {
            while (this.hasWork() && this.queuesPerPriority.get(this.topPriorityQueueIndex).isEmpty()) {
                ++this.topPriorityQueueIndex;
            }
        }
        if (list != null && !list.isEmpty()) {
            ((List)this.queuesPerPriority.get(n2).computeIfAbsent(chunkPos.toLong(), l -> Lists.newArrayList())).addAll(list);
            this.topPriorityQueueIndex = Math.min(this.topPriorityQueueIndex, n2);
        }
    }

    protected void submit(Runnable runnable, long l2, int n) {
        ((List)this.queuesPerPriority.get(n).computeIfAbsent(l2, l -> Lists.newArrayList())).add(runnable);
        this.topPriorityQueueIndex = Math.min(this.topPriorityQueueIndex, n);
    }

    protected void release(long l, boolean bl) {
        for (Long2ObjectLinkedOpenHashMap<List<Runnable>> long2ObjectLinkedOpenHashMap : this.queuesPerPriority) {
            List list = (List)long2ObjectLinkedOpenHashMap.get(l);
            if (list == null) continue;
            if (bl) {
                list.clear();
            }
            if (!list.isEmpty()) continue;
            long2ObjectLinkedOpenHashMap.remove(l);
        }
        while (this.hasWork() && this.queuesPerPriority.get(this.topPriorityQueueIndex).isEmpty()) {
            ++this.topPriorityQueueIndex;
        }
    }

    @Nullable
    public TasksForChunk pop() {
        if (!this.hasWork()) {
            return null;
        }
        int n = this.topPriorityQueueIndex;
        Long2ObjectLinkedOpenHashMap<List<Runnable>> long2ObjectLinkedOpenHashMap = this.queuesPerPriority.get(n);
        long l = long2ObjectLinkedOpenHashMap.firstLongKey();
        List list = (List)long2ObjectLinkedOpenHashMap.removeFirst();
        while (this.hasWork() && this.queuesPerPriority.get(this.topPriorityQueueIndex).isEmpty()) {
            ++this.topPriorityQueueIndex;
        }
        return new TasksForChunk(l, list);
    }

    public boolean hasWork() {
        return this.topPriorityQueueIndex < PRIORITY_LEVEL_COUNT;
    }

    public String toString() {
        return this.name + " " + this.topPriorityQueueIndex + "...";
    }

    public record TasksForChunk(long chunkPos, List<Runnable> tasks) {
    }
}

