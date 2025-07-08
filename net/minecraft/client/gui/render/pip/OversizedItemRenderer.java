/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.render.pip;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.GuiItemRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.OversizedItemRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class OversizedItemRenderer
extends PictureInPictureRenderer<OversizedItemRenderState> {
    private boolean usedOnThisFrame;
    @Nullable
    private Object modelOnTextureIdentity;

    public OversizedItemRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    public boolean usedOnThisFrame() {
        return this.usedOnThisFrame;
    }

    public void resetUsedOnThisFrame() {
        this.usedOnThisFrame = false;
    }

    public void invalidateTexture() {
        this.modelOnTextureIdentity = null;
    }

    @Override
    public Class<OversizedItemRenderState> getRenderStateClass() {
        return OversizedItemRenderState.class;
    }

    @Override
    protected void renderToTexture(OversizedItemRenderState oversizedItemRenderState, PoseStack poseStack) {
        boolean bl;
        poseStack.scale(1.0f, -1.0f, -1.0f);
        GuiItemRenderState guiItemRenderState = oversizedItemRenderState.guiItemRenderState();
        ScreenRectangle screenRectangle = guiItemRenderState.oversizedItemBounds();
        Objects.requireNonNull(screenRectangle);
        float f = (float)(screenRectangle.left() + screenRectangle.right()) / 2.0f;
        float f2 = (float)(screenRectangle.top() + screenRectangle.bottom()) / 2.0f;
        float f3 = (float)guiItemRenderState.x() + 8.0f;
        float f4 = (float)guiItemRenderState.y() + 8.0f;
        poseStack.translate((f3 - f) / 16.0f, (f2 - f4) / 16.0f, 0.0f);
        TrackingItemStackRenderState trackingItemStackRenderState = guiItemRenderState.itemStackRenderState();
        boolean bl2 = bl = !trackingItemStackRenderState.usesBlockLight();
        if (bl) {
            Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_FLAT);
        } else {
            Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
        }
        trackingItemStackRenderState.render(poseStack, this.bufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY);
        this.modelOnTextureIdentity = trackingItemStackRenderState.getModelIdentity();
    }

    @Override
    public void blitTexture(OversizedItemRenderState oversizedItemRenderState, GuiRenderState guiRenderState) {
        super.blitTexture(oversizedItemRenderState, guiRenderState);
        this.usedOnThisFrame = true;
    }

    @Override
    public boolean textureIsReadyToBlit(OversizedItemRenderState oversizedItemRenderState) {
        TrackingItemStackRenderState trackingItemStackRenderState = oversizedItemRenderState.guiItemRenderState().itemStackRenderState();
        return !trackingItemStackRenderState.isAnimated() && trackingItemStackRenderState.getModelIdentity().equals(this.modelOnTextureIdentity);
    }

    @Override
    protected float getTranslateY(int n, int n2) {
        return (float)n / 2.0f;
    }

    @Override
    protected String getTextureLabel() {
        return "oversized_item";
    }
}

