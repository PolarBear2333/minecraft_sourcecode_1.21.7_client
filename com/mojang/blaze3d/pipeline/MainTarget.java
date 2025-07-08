/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  javax.annotation.Nullable
 */
package com.mojang.blaze3d.pipeline;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.GpuOutOfMemoryException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class MainTarget
extends RenderTarget {
    public static final int DEFAULT_WIDTH = 854;
    public static final int DEFAULT_HEIGHT = 480;
    static final Dimension DEFAULT_DIMENSIONS = new Dimension(854, 480);

    public MainTarget(int n, int n2) {
        super("Main", true);
        this.createFrameBuffer(n, n2);
    }

    private void createFrameBuffer(int n, int n2) {
        Dimension dimension = this.allocateAttachments(n, n2);
        if (this.colorTexture == null || this.depthTexture == null) {
            throw new IllegalStateException("Missing color and/or depth textures");
        }
        this.colorTexture.setTextureFilter(FilterMode.NEAREST, false);
        this.colorTexture.setAddressMode(AddressMode.CLAMP_TO_EDGE);
        this.colorTexture.setTextureFilter(FilterMode.NEAREST, false);
        this.colorTexture.setAddressMode(AddressMode.CLAMP_TO_EDGE);
        this.viewWidth = dimension.width;
        this.viewHeight = dimension.height;
        this.width = dimension.width;
        this.height = dimension.height;
    }

    private Dimension allocateAttachments(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        for (Dimension dimension : Dimension.listWithFallback(n, n2)) {
            if (this.colorTexture != null) {
                this.colorTexture.close();
                this.colorTexture = null;
            }
            if (this.colorTextureView != null) {
                this.colorTextureView.close();
                this.colorTextureView = null;
            }
            if (this.depthTexture != null) {
                this.depthTexture.close();
                this.depthTexture = null;
            }
            if (this.depthTextureView != null) {
                this.depthTextureView.close();
                this.depthTextureView = null;
            }
            this.colorTexture = this.allocateColorAttachment(dimension);
            this.depthTexture = this.allocateDepthAttachment(dimension);
            if (this.colorTexture == null || this.depthTexture == null) continue;
            this.colorTextureView = RenderSystem.getDevice().createTextureView(this.colorTexture);
            this.depthTextureView = RenderSystem.getDevice().createTextureView(this.depthTexture);
            return dimension;
        }
        throw new RuntimeException("Unrecoverable GL_OUT_OF_MEMORY (" + (this.colorTexture == null ? "missing color" : "have color") + ", " + (this.depthTexture == null ? "missing depth" : "have depth") + ")");
    }

    @Nullable
    private GpuTexture allocateColorAttachment(Dimension dimension) {
        try {
            return RenderSystem.getDevice().createTexture(() -> this.label + " / Color", 15, TextureFormat.RGBA8, dimension.width, dimension.height, 1, 1);
        }
        catch (GpuOutOfMemoryException gpuOutOfMemoryException) {
            return null;
        }
    }

    @Nullable
    private GpuTexture allocateDepthAttachment(Dimension dimension) {
        try {
            return RenderSystem.getDevice().createTexture(() -> this.label + " / Depth", 15, TextureFormat.DEPTH32, dimension.width, dimension.height, 1, 1);
        }
        catch (GpuOutOfMemoryException gpuOutOfMemoryException) {
            return null;
        }
    }

    static class Dimension {
        public final int width;
        public final int height;

        Dimension(int n, int n2) {
            this.width = n;
            this.height = n2;
        }

        static List<Dimension> listWithFallback(int n, int n2) {
            RenderSystem.assertOnRenderThread();
            int n3 = RenderSystem.getDevice().getMaxTextureSize();
            if (n <= 0 || n > n3 || n2 <= 0 || n2 > n3) {
                return ImmutableList.of((Object)DEFAULT_DIMENSIONS);
            }
            return ImmutableList.of((Object)new Dimension(n, n2), (Object)DEFAULT_DIMENSIONS);
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            Dimension dimension = (Dimension)object;
            return this.width == dimension.width && this.height == dimension.height;
        }

        public int hashCode() {
            return Objects.hash(this.width, this.height);
        }

        public String toString() {
            return this.width + "x" + this.height;
        }
    }
}

