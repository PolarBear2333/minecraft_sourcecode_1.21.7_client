/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

public class DynamicUniformStorage<T extends DynamicUniform>
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final List<MappableRingBuffer> oldBuffers = new ArrayList<MappableRingBuffer>();
    private final int blockSize;
    private MappableRingBuffer ringBuffer;
    private int nextBlock;
    private int capacity;
    @Nullable
    private T lastUniform;
    private final String label;

    public DynamicUniformStorage(String string, int n, int n2) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        this.blockSize = Mth.roundToward(n, gpuDevice.getUniformOffsetAlignment());
        this.capacity = Mth.smallestEncompassingPowerOfTwo(n2);
        this.nextBlock = 0;
        this.ringBuffer = new MappableRingBuffer(() -> string + " x" + this.blockSize, 130, this.blockSize * this.capacity);
        this.label = string;
    }

    public void endFrame() {
        this.nextBlock = 0;
        this.lastUniform = null;
        this.ringBuffer.rotate();
        if (!this.oldBuffers.isEmpty()) {
            for (MappableRingBuffer mappableRingBuffer : this.oldBuffers) {
                mappableRingBuffer.close();
            }
            this.oldBuffers.clear();
        }
    }

    private void resizeBuffers(int n) {
        this.capacity = n;
        this.nextBlock = 0;
        this.lastUniform = null;
        this.oldBuffers.add(this.ringBuffer);
        this.ringBuffer = new MappableRingBuffer(() -> this.label + " x" + this.blockSize, 130, this.blockSize * this.capacity);
    }

    public GpuBufferSlice writeUniform(T t) {
        int n;
        if (this.lastUniform != null && this.lastUniform.equals(t)) {
            return this.ringBuffer.currentBuffer().slice((this.nextBlock - 1) * this.blockSize, this.blockSize);
        }
        if (this.nextBlock >= this.capacity) {
            n = this.capacity * 2;
            LOGGER.info("Resizing " + this.label + ", capacity limit of {} reached during a single frame. New capacity will be {}.", (Object)this.capacity, (Object)n);
            this.resizeBuffers(n);
        }
        n = this.nextBlock * this.blockSize;
        try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.ringBuffer.currentBuffer().slice(n, this.blockSize), false, true);){
            t.write(mappedView.data());
        }
        ++this.nextBlock;
        this.lastUniform = t;
        return this.ringBuffer.currentBuffer().slice(n, this.blockSize);
    }

    public GpuBufferSlice[] writeUniforms(T[] TArray) {
        int n;
        if (TArray.length == 0) {
            return new GpuBufferSlice[0];
        }
        if (this.nextBlock + TArray.length > this.capacity) {
            n = Mth.smallestEncompassingPowerOfTwo(Math.max(this.capacity + 1, TArray.length));
            LOGGER.info("Resizing " + this.label + ", capacity limit of {} reached during a single frame. New capacity will be {}.", (Object)this.capacity, (Object)n);
            this.resizeBuffers(n);
        }
        n = this.nextBlock * this.blockSize;
        GpuBufferSlice[] gpuBufferSliceArray = new GpuBufferSlice[TArray.length];
        try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.ringBuffer.currentBuffer().slice(n, TArray.length * this.blockSize), false, true);){
            ByteBuffer byteBuffer = mappedView.data();
            for (int i = 0; i < TArray.length; ++i) {
                T t = TArray[i];
                gpuBufferSliceArray[i] = this.ringBuffer.currentBuffer().slice(n + i * this.blockSize, this.blockSize);
                byteBuffer.position(i * this.blockSize);
                t.write(byteBuffer);
            }
        }
        this.nextBlock += TArray.length;
        this.lastUniform = TArray[TArray.length - 1];
        return gpuBufferSliceArray;
    }

    @Override
    public void close() {
        for (MappableRingBuffer mappableRingBuffer : this.oldBuffers) {
            mappableRingBuffer.close();
        }
        this.ringBuffer.close();
    }

    public static interface DynamicUniform {
        public void write(ByteBuffer var1);
    }
}

