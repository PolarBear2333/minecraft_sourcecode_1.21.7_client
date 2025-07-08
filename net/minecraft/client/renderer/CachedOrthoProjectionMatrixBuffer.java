/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.lwjgl.system.MemoryStack
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

public class CachedOrthoProjectionMatrixBuffer
implements AutoCloseable {
    private final GpuBuffer buffer;
    private final GpuBufferSlice bufferSlice;
    private final float zNear;
    private final float zFar;
    private final boolean invertY;
    private float width;
    private float height;

    public CachedOrthoProjectionMatrixBuffer(String string, float f, float f2, boolean bl) {
        this.zNear = f;
        this.zFar = f2;
        this.invertY = bl;
        GpuDevice gpuDevice = RenderSystem.getDevice();
        this.buffer = gpuDevice.createBuffer(() -> "Projection matrix UBO " + string, 136, RenderSystem.PROJECTION_MATRIX_UBO_SIZE);
        this.bufferSlice = this.buffer.slice(0, RenderSystem.PROJECTION_MATRIX_UBO_SIZE);
    }

    public GpuBufferSlice getBuffer(float f, float f2) {
        if (this.width != f || this.height != f2) {
            Matrix4f matrix4f = this.createProjectionMatrix(f, f2);
            try (MemoryStack memoryStack = MemoryStack.stackPush();){
                ByteBuffer byteBuffer = Std140Builder.onStack(memoryStack, RenderSystem.PROJECTION_MATRIX_UBO_SIZE).putMat4f((Matrix4fc)matrix4f).get();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.buffer.slice(), byteBuffer);
            }
            this.width = f;
            this.height = f2;
        }
        return this.bufferSlice;
    }

    private Matrix4f createProjectionMatrix(float f, float f2) {
        return new Matrix4f().setOrtho(0.0f, f, this.invertY ? f2 : 0.0f, this.invertY ? 0.0f : f2, this.zNear, this.zFar);
    }

    @Override
    public void close() {
        this.buffer.close();
    }
}

