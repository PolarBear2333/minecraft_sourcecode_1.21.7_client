/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.render.pip;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.GuiBannerResultRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;

public class GuiBannerResultRenderer
extends PictureInPictureRenderer<GuiBannerResultRenderState> {
    public GuiBannerResultRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<GuiBannerResultRenderState> getRenderStateClass() {
        return GuiBannerResultRenderState.class;
    }

    @Override
    protected void renderToTexture(GuiBannerResultRenderState guiBannerResultRenderState, PoseStack poseStack) {
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_FLAT);
        poseStack.translate(0.0f, 0.25f, 0.0f);
        BannerRenderer.renderPatterns(poseStack, this.bufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY, guiBannerResultRenderState.flag(), ModelBakery.BANNER_BASE, true, guiBannerResultRenderState.baseColor(), guiBannerResultRenderState.resultBannerPatterns());
    }

    @Override
    protected String getTextureLabel() {
        return "banner result";
    }
}

