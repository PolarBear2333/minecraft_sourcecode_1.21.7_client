/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Supplier;

public class MappableRingBuffer
implements AutoCloseable {
    private static final int BUFFER_COUNT = 3;
    private final GpuBuffer[] buffers = new GpuBuffer[3];
    private final GpuFence[] fences = new GpuFence[3];
    private final int size;
    private int current = 0;

    public MappableRingBuffer(Supplier<String> supplier, int n, int n2) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        if ((n & 1) == 0 && (n & 2) == 0) {
            throw new IllegalArgumentException("MappableRingBuffer requires at least one of USAGE_MAP_READ or USAGE_MAP_WRITE");
        }
        for (int i = 0; i < 3; ++i) {
            int n3 = i;
            this.buffers[i] = gpuDevice.createBuffer(() -> (String)supplier.get() + " #" + n3, n, n2);
            this.fences[i] = null;
        }
        this.size = n2;
    }

    public int size() {
        return this.size;
    }

    public GpuBuffer currentBuffer() {
        GpuFence gpuFence = this.fences[this.current];
        if (gpuFence != null) {
            gpuFence.awaitCompletion(Long.MAX_VALUE);
            gpuFence.close();
            this.fences[this.current] = null;
        }
        return this.buffers[this.current];
    }

    public void rotate() {
        if (this.fences[this.current] != null) {
            this.fences[this.current].close();
        }
        this.fences[this.current] = RenderSystem.getDevice().createCommandEncoder().createFence();
        this.current = (this.current + 1) % 3;
    }

    @Override
    public void close() {
        for (int i = 0; i < 3; ++i) {
            this.buffers[i].close();
            if (this.fences[i] == null) continue;
            this.fences[i].close();
        }
    }
}

