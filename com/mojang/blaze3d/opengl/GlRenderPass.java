/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;

public class GlRenderPass
implements RenderPass {
    protected static final int MAX_VERTEX_BUFFERS = 1;
    public static final boolean VALIDATION = SharedConstants.IS_RUNNING_IN_IDE;
    private final GlCommandEncoder encoder;
    private final boolean hasDepthTexture;
    private boolean closed;
    @Nullable
    protected GlRenderPipeline pipeline;
    protected final GpuBuffer[] vertexBuffers = new GpuBuffer[1];
    @Nullable
    protected GpuBuffer indexBuffer;
    protected VertexFormat.IndexType indexType = VertexFormat.IndexType.INT;
    private final ScissorState scissorState = new ScissorState();
    protected final HashMap<String, GpuBufferSlice> uniforms = new HashMap();
    protected final HashMap<String, GpuTextureView> samplers = new HashMap();
    protected final Set<String> dirtyUniforms = new HashSet<String>();
    protected int pushedDebugGroups;

    public GlRenderPass(GlCommandEncoder glCommandEncoder, boolean bl) {
        this.encoder = glCommandEncoder;
        this.hasDepthTexture = bl;
    }

    public boolean hasDepthTexture() {
        return this.hasDepthTexture;
    }

    @Override
    public void pushDebugGroup(Supplier<String> supplier) {
        if (this.closed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        ++this.pushedDebugGroups;
        this.encoder.getDevice().debugLabels().pushDebugGroup(supplier);
    }

    @Override
    public void popDebugGroup() {
        if (this.closed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        if (this.pushedDebugGroups == 0) {
            throw new IllegalStateException("Can't pop more debug groups than was pushed!");
        }
        --this.pushedDebugGroups;
        this.encoder.getDevice().debugLabels().popDebugGroup();
    }

    @Override
    public void setPipeline(RenderPipeline renderPipeline) {
        if (this.pipeline == null || this.pipeline.info() != renderPipeline) {
            this.dirtyUniforms.addAll(this.uniforms.keySet());
            this.dirtyUniforms.addAll(this.samplers.keySet());
        }
        this.pipeline = this.encoder.getDevice().getOrCompilePipeline(renderPipeline);
    }

    @Override
    public void bindSampler(String string, @Nullable GpuTextureView gpuTextureView) {
        if (gpuTextureView == null) {
            this.samplers.remove(string);
        } else {
            this.samplers.put(string, gpuTextureView);
        }
        this.dirtyUniforms.add(string);
    }

    @Override
    public void setUniform(String string, GpuBuffer gpuBuffer) {
        this.uniforms.put(string, gpuBuffer.slice());
        this.dirtyUniforms.add(string);
    }

    @Override
    public void setUniform(String string, GpuBufferSlice gpuBufferSlice) {
        int n = this.encoder.getDevice().getUniformOffsetAlignment();
        if (gpuBufferSlice.offset() % n > 0) {
            throw new IllegalArgumentException("Uniform buffer offset must be aligned to " + n);
        }
        this.uniforms.put(string, gpuBufferSlice);
        this.dirtyUniforms.add(string);
    }

    @Override
    public void enableScissor(int n, int n2, int n3, int n4) {
        this.scissorState.enable(n, n2, n3, n4);
    }

    @Override
    public void disableScissor() {
        this.scissorState.disable();
    }

    public boolean isScissorEnabled() {
        return this.scissorState.enabled();
    }

    public int getScissorX() {
        return this.scissorState.x();
    }

    public int getScissorY() {
        return this.scissorState.y();
    }

    public int getScissorWidth() {
        return this.scissorState.width();
    }

    public int getScissorHeight() {
        return this.scissorState.height();
    }

    @Override
    public void setVertexBuffer(int n, GpuBuffer gpuBuffer) {
        if (n < 0 || n >= 1) {
            throw new IllegalArgumentException("Vertex buffer slot is out of range: " + n);
        }
        this.vertexBuffers[n] = gpuBuffer;
    }

    @Override
    public void setIndexBuffer(@Nullable GpuBuffer gpuBuffer, VertexFormat.IndexType indexType) {
        this.indexBuffer = gpuBuffer;
        this.indexType = indexType;
    }

    @Override
    public void drawIndexed(int n, int n2, int n3, int n4) {
        if (this.closed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        this.encoder.executeDraw(this, n, n2, n3, this.indexType, n4);
    }

    @Override
    public <T> void drawMultipleIndexed(Collection<RenderPass.Draw<T>> collection, @Nullable GpuBuffer gpuBuffer, @Nullable VertexFormat.IndexType indexType, Collection<String> collection2, T t) {
        if (this.closed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        this.encoder.executeDrawMultiple(this, collection, gpuBuffer, indexType, collection2, t);
    }

    @Override
    public void draw(int n, int n2) {
        if (this.closed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        this.encoder.executeDraw(this, n, 0, n2, null, 1);
    }

    @Override
    public void close() {
        if (!this.closed) {
            if (this.pushedDebugGroups > 0) {
                throw new IllegalStateException("Render pass had debug groups left open!");
            }
            this.closed = true;
            this.encoder.finishRenderPass();
        }
    }
}

