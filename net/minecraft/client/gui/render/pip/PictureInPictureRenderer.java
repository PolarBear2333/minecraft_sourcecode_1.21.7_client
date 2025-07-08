/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.render.pip;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;

public abstract class PictureInPictureRenderer<T extends PictureInPictureRenderState>
implements AutoCloseable {
    protected final MultiBufferSource.BufferSource bufferSource;
    @Nullable
    private GpuTexture texture;
    @Nullable
    private GpuTextureView textureView;
    @Nullable
    private GpuTexture depthTexture;
    @Nullable
    private GpuTextureView depthTextureView;
    private final CachedOrthoProjectionMatrixBuffer projectionMatrixBuffer = new CachedOrthoProjectionMatrixBuffer("PIP - " + this.getClass().getSimpleName(), -1000.0f, 1000.0f, true);

    protected PictureInPictureRenderer(MultiBufferSource.BufferSource bufferSource) {
        this.bufferSource = bufferSource;
    }

    public void prepare(T t, GuiRenderState guiRenderState, int n) {
        boolean bl;
        int n2 = (t.x1() - t.x0()) * n;
        int n3 = (t.y1() - t.y0()) * n;
        boolean bl2 = bl = this.texture == null || this.texture.getWidth(0) != n2 || this.texture.getHeight(0) != n3;
        if (!bl && this.textureIsReadyToBlit(t)) {
            this.blitTexture(t, guiRenderState);
            return;
        }
        this.prepareTexturesAndProjection(bl, n2, n3);
        RenderSystem.outputColorTextureOverride = this.textureView;
        RenderSystem.outputDepthTextureOverride = this.depthTextureView;
        PoseStack poseStack = new PoseStack();
        poseStack.translate((float)n2 / 2.0f, this.getTranslateY(n3, n), 0.0f);
        float f = (float)n * t.scale();
        poseStack.scale(f, f, -f);
        this.renderToTexture(t, poseStack);
        this.bufferSource.endBatch();
        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;
        this.blitTexture(t, guiRenderState);
    }

    protected void blitTexture(T t, GuiRenderState guiRenderState) {
        guiRenderState.submitBlitToCurrentLayer(new BlitRenderState(RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA, TextureSetup.singleTexture(this.textureView), t.pose(), t.x0(), t.y0(), t.x1(), t.y1(), 0.0f, 1.0f, 1.0f, 0.0f, -1, t.scissorArea(), null));
    }

    private void prepareTexturesAndProjection(boolean bl, int n, int n2) {
        if (this.texture != null && bl) {
            this.texture.close();
            this.texture = null;
            this.textureView.close();
            this.textureView = null;
            this.depthTexture.close();
            this.depthTexture = null;
            this.depthTextureView.close();
            this.depthTextureView = null;
        }
        GpuDevice gpuDevice = RenderSystem.getDevice();
        if (this.texture == null) {
            this.texture = gpuDevice.createTexture(() -> "UI " + this.getTextureLabel() + " texture", 12, TextureFormat.RGBA8, n, n2, 1, 1);
            this.texture.setTextureFilter(FilterMode.NEAREST, false);
            this.textureView = gpuDevice.createTextureView(this.texture);
            this.depthTexture = gpuDevice.createTexture(() -> "UI " + this.getTextureLabel() + " depth texture", 8, TextureFormat.DEPTH32, n, n2, 1, 1);
            this.depthTextureView = gpuDevice.createTextureView(this.depthTexture);
        }
        gpuDevice.createCommandEncoder().clearColorAndDepthTextures(this.texture, 0, this.depthTexture, 1.0);
        RenderSystem.setProjectionMatrix(this.projectionMatrixBuffer.getBuffer(n, n2), ProjectionType.ORTHOGRAPHIC);
    }

    protected boolean textureIsReadyToBlit(T t) {
        return false;
    }

    protected float getTranslateY(int n, int n2) {
        return n;
    }

    @Override
    public void close() {
        if (this.texture != null) {
            this.texture.close();
        }
        if (this.textureView != null) {
            this.textureView.close();
        }
        if (this.depthTexture != null) {
            this.depthTexture.close();
        }
        if (this.depthTextureView != null) {
            this.depthTextureView.close();
        }
        this.projectionMatrixBuffer.close();
    }

    public abstract Class<T> getRenderStateClass();

    protected abstract void renderToTexture(T var1, PoseStack var2);

    protected abstract String getTextureLabel();
}

