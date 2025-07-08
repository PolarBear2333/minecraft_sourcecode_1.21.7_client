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

public class CachedPerspectiveProjectionMatrixBuffer
implements AutoCloseable {
    private final GpuBuffer buffer;
    private final GpuBufferSlice bufferSlice;
    private final float zNear;
    private final float zFar;
    private int width;
    private int height;
    private float fov;

    public CachedPerspectiveProjectionMatrixBuffer(String string, float f, float f2) {
        this.zNear = f;
        this.zFar = f2;
        GpuDevice gpuDevice = RenderSystem.getDevice();
        this.buffer = gpuDevice.createBuffer(() -> "Projection matrix UBO " + string, 136, RenderSystem.PROJECTION_MATRIX_UBO_SIZE);
        this.bufferSlice = this.buffer.slice(0, RenderSystem.PROJECTION_MATRIX_UBO_SIZE);
    }

    public GpuBufferSlice getBuffer(int n, int n2, float f) {
        if (this.width != n || this.height != n2 || this.fov != f) {
            Matrix4f matrix4f = this.createProjectionMatrix(n, n2, f);
            try (MemoryStack memoryStack = MemoryStack.stackPush();){
                ByteBuffer byteBuffer = Std140Builder.onStack(memoryStack, RenderSystem.PROJECTION_MATRIX_UBO_SIZE).putMat4f((Matrix4fc)matrix4f).get();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.buffer.slice(), byteBuffer);
            }
            this.width = n;
            this.height = n2;
            this.fov = f;
        }
        return this.bufferSlice;
    }

    private Matrix4f createProjectionMatrix(int n, int n2, float f) {
        return new Matrix4f().perspective(f * ((float)Math.PI / 180), (float)n / (float)n2, this.zNear, this.zFar);
    }

    @Override
    public void close() {
        this.buffer.close();
    }
}

