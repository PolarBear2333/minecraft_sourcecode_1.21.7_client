/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Queues
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer;

import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import org.slf4j.Logger;

public class SectionBufferBuilderPool {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Queue<SectionBufferBuilderPack> freeBuffers;
    private volatile int freeBufferCount;

    private SectionBufferBuilderPool(List<SectionBufferBuilderPack> list) {
        this.freeBuffers = Queues.newArrayDeque(list);
        this.freeBufferCount = this.freeBuffers.size();
    }

    public static SectionBufferBuilderPool allocate(int n) {
        int n2 = Math.max(1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3) / SectionBufferBuilderPack.TOTAL_BUFFERS_SIZE);
        int n3 = Math.max(1, Math.min(n, n2));
        ArrayList<SectionBufferBuilderPack> arrayList = new ArrayList<SectionBufferBuilderPack>(n3);
        try {
            for (int i = 0; i < n3; ++i) {
                arrayList.add(new SectionBufferBuilderPack());
            }
        }
        catch (OutOfMemoryError outOfMemoryError) {
            LOGGER.warn("Allocated only {}/{} buffers", (Object)arrayList.size(), (Object)n3);
            int n4 = Math.min(arrayList.size() * 2 / 3, arrayList.size() - 1);
            for (int i = 0; i < n4; ++i) {
                ((SectionBufferBuilderPack)arrayList.remove(arrayList.size() - 1)).close();
            }
        }
        return new SectionBufferBuilderPool(arrayList);
    }

    @Nullable
    public SectionBufferBuilderPack acquire() {
        SectionBufferBuilderPack sectionBufferBuilderPack = this.freeBuffers.poll();
        if (sectionBufferBuilderPack != null) {
            this.freeBufferCount = this.freeBuffers.size();
            return sectionBufferBuilderPack;
        }
        return null;
    }

    public void release(SectionBufferBuilderPack sectionBufferBuilderPack) {
        this.freeBuffers.add(sectionBufferBuilderPack);
        this.freeBufferCount = this.freeBuffers.size();
    }

    public boolean isEmpty() {
        return this.freeBuffers.isEmpty();
    }

    public int getFreeBufferCount() {
        return this.freeBufferCount;
    }
}

