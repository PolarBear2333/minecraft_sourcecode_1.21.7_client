/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.chunk;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import javax.annotation.Nullable;

public final class SectionBuffers
implements AutoCloseable {
    private GpuBuffer vertexBuffer;
    @Nullable
    private GpuBuffer indexBuffer;
    private int indexCount;
    private VertexFormat.IndexType indexType;

    public SectionBuffers(GpuBuffer gpuBuffer, @Nullable GpuBuffer gpuBuffer2, int n, VertexFormat.IndexType indexType) {
        this.vertexBuffer = gpuBuffer;
        this.indexBuffer = gpuBuffer2;
        this.indexCount = n;
        this.indexType = indexType;
    }

    public GpuBuffer getVertexBuffer() {
        return this.vertexBuffer;
    }

    @Nullable
    public GpuBuffer getIndexBuffer() {
        return this.indexBuffer;
    }

    public void setIndexBuffer(@Nullable GpuBuffer gpuBuffer) {
        this.indexBuffer = gpuBuffer;
    }

    public int getIndexCount() {
        return this.indexCount;
    }

    public VertexFormat.IndexType getIndexType() {
        return this.indexType;
    }

    public void setIndexType(VertexFormat.IndexType indexType) {
        this.indexType = indexType;
    }

    public void setIndexCount(int n) {
        this.indexCount = n;
    }

    public void setVertexBuffer(GpuBuffer gpuBuffer) {
        this.vertexBuffer = gpuBuffer;
    }

    @Override
    public void close() {
        this.vertexBuffer.close();
        if (this.indexBuffer != null) {
            this.indexBuffer.close();
        }
    }
}

