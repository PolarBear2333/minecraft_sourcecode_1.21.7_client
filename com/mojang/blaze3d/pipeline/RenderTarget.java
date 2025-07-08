/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderPipelines;

public abstract class RenderTarget {
    private static int UNNAMED_RENDER_TARGETS = 0;
    public int width;
    public int height;
    public int viewWidth;
    public int viewHeight;
    protected final String label;
    public final boolean useDepth;
    @Nullable
    protected GpuTexture colorTexture;
    @Nullable
    protected GpuTextureView colorTextureView;
    @Nullable
    protected GpuTexture depthTexture;
    @Nullable
    protected GpuTextureView depthTextureView;
    public FilterMode filterMode;

    public RenderTarget(@Nullable String string, boolean bl) {
        this.label = string == null ? "FBO " + UNNAMED_RENDER_TARGETS++ : string;
        this.useDepth = bl;
    }

    public void resize(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        this.destroyBuffers();
        this.createBuffers(n, n2);
    }

    public void destroyBuffers() {
        RenderSystem.assertOnRenderThread();
        if (this.depthTexture != null) {
            this.depthTexture.close();
            this.depthTexture = null;
        }
        if (this.depthTextureView != null) {
            this.depthTextureView.close();
            this.depthTextureView = null;
        }
        if (this.colorTexture != null) {
            this.colorTexture.close();
            this.colorTexture = null;
        }
        if (this.colorTextureView != null) {
            this.colorTextureView.close();
            this.colorTextureView = null;
        }
    }

    public void copyDepthFrom(RenderTarget renderTarget) {
        RenderSystem.assertOnRenderThread();
        if (this.depthTexture == null) {
            throw new IllegalStateException("Trying to copy depth texture to a RenderTarget without a depth texture");
        }
        if (renderTarget.depthTexture == null) {
            throw new IllegalStateException("Trying to copy depth texture from a RenderTarget without a depth texture");
        }
        RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(renderTarget.depthTexture, this.depthTexture, 0, 0, 0, 0, 0, this.width, this.height);
    }

    public void createBuffers(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        GpuDevice gpuDevice = RenderSystem.getDevice();
        int n3 = gpuDevice.getMaxTextureSize();
        if (n <= 0 || n > n3 || n2 <= 0 || n2 > n3) {
            throw new IllegalArgumentException("Window " + n + "x" + n2 + " size out of bounds (max. size: " + n3 + ")");
        }
        this.viewWidth = n;
        this.viewHeight = n2;
        this.width = n;
        this.height = n2;
        if (this.useDepth) {
            this.depthTexture = gpuDevice.createTexture(() -> this.label + " / Depth", 15, TextureFormat.DEPTH32, n, n2, 1, 1);
            this.depthTextureView = gpuDevice.createTextureView(this.depthTexture);
            this.depthTexture.setTextureFilter(FilterMode.NEAREST, false);
            this.depthTexture.setAddressMode(AddressMode.CLAMP_TO_EDGE);
        }
        this.colorTexture = gpuDevice.createTexture(() -> this.label + " / Color", 15, TextureFormat.RGBA8, n, n2, 1, 1);
        this.colorTextureView = gpuDevice.createTextureView(this.colorTexture);
        this.colorTexture.setAddressMode(AddressMode.CLAMP_TO_EDGE);
        this.setFilterMode(FilterMode.NEAREST, true);
    }

    public void setFilterMode(FilterMode filterMode) {
        this.setFilterMode(filterMode, false);
    }

    private void setFilterMode(FilterMode filterMode, boolean bl) {
        if (this.colorTexture == null) {
            throw new IllegalStateException("Can't change filter mode, color texture doesn't exist yet");
        }
        if (bl || filterMode != this.filterMode) {
            this.filterMode = filterMode;
            this.colorTexture.setTextureFilter(filterMode, false);
        }
    }

    public void blitToScreen() {
        if (this.colorTexture == null) {
            throw new IllegalStateException("Can't blit to screen, color texture doesn't exist yet");
        }
        RenderSystem.getDevice().createCommandEncoder().presentTexture(this.colorTextureView);
    }

    public void blitAndBlendToTexture(GpuTextureView gpuTextureView) {
        RenderSystem.assertOnRenderThread();
        RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer gpuBuffer = autoStorageIndexBuffer.getBuffer(6);
        GpuBuffer gpuBuffer2 = RenderSystem.getQuadVertexBuffer();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Blit render target", gpuTextureView, OptionalInt.empty());){
            renderPass.setPipeline(RenderPipelines.ENTITY_OUTLINE_BLIT);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setVertexBuffer(0, gpuBuffer2);
            renderPass.setIndexBuffer(gpuBuffer, autoStorageIndexBuffer.type());
            renderPass.bindSampler("InSampler", this.colorTextureView);
            renderPass.drawIndexed(0, 0, 6, 1);
        }
    }

    @Nullable
    public GpuTexture getColorTexture() {
        return this.colorTexture;
    }

    @Nullable
    public GpuTextureView getColorTextureView() {
        return this.colorTextureView;
    }

    @Nullable
    public GpuTexture getDepthTexture() {
        return this.depthTexture;
    }

    @Nullable
    public GpuTextureView getDepthTextureView() {
        return this.depthTextureView;
    }
}

