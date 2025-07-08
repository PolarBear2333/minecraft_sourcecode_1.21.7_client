/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.chunk;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.world.phys.Vec3;

public class CompileTaskDynamicQueue {
    private static final int MAX_RECOMPILE_QUOTA = 2;
    private int recompileQuota = 2;
    private final List<SectionRenderDispatcher.RenderSection.CompileTask> tasks = new ObjectArrayList();
    private volatile int size = 0;

    public synchronized void add(SectionRenderDispatcher.RenderSection.CompileTask compileTask) {
        this.tasks.add(compileTask);
        ++this.size;
    }

    @Nullable
    public synchronized SectionRenderDispatcher.RenderSection.CompileTask poll(Vec3 vec3) {
        boolean bl;
        int n;
        int n2 = -1;
        int n3 = -1;
        double d = Double.MAX_VALUE;
        double d2 = Double.MAX_VALUE;
        ListIterator<SectionRenderDispatcher.RenderSection.CompileTask> listIterator = this.tasks.listIterator();
        while (listIterator.hasNext()) {
            n = listIterator.nextIndex();
            SectionRenderDispatcher.RenderSection.CompileTask compileTask = listIterator.next();
            if (compileTask.isCancelled.get()) {
                listIterator.remove();
                continue;
            }
            double d3 = compileTask.getRenderOrigin().distToCenterSqr(vec3);
            if (!compileTask.isRecompile() && d3 < d) {
                d = d3;
                n2 = n;
            }
            if (!compileTask.isRecompile() || !(d3 < d2)) continue;
            d2 = d3;
            n3 = n;
        }
        n = n3 >= 0 ? 1 : 0;
        boolean bl2 = bl = n2 >= 0;
        if (n != 0 && (!bl || this.recompileQuota > 0 && d2 < d)) {
            --this.recompileQuota;
            return this.removeTaskByIndex(n3);
        }
        this.recompileQuota = 2;
        return this.removeTaskByIndex(n2);
    }

    public int size() {
        return this.size;
    }

    @Nullable
    private SectionRenderDispatcher.RenderSection.CompileTask removeTaskByIndex(int n) {
        if (n >= 0) {
            --this.size;
            return this.tasks.remove(n);
        }
        return null;
    }

    public synchronized void clear() {
        for (SectionRenderDispatcher.RenderSection.CompileTask compileTask : this.tasks) {
            compileTask.cancel();
        }
        this.tasks.clear();
        this.size = 0;
    }
}

