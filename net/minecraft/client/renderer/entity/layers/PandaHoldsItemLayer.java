/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PandaRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

public class PandaHoldsItemLayer
extends RenderLayer<PandaRenderState, PandaModel> {
    public PandaHoldsItemLayer(RenderLayerParent<PandaRenderState, PandaModel> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, PandaRenderState pandaRenderState, float f, float f2) {
        ItemStackRenderState itemStackRenderState = pandaRenderState.heldItem;
        if (itemStackRenderState.isEmpty() || !pandaRenderState.isSitting || pandaRenderState.isScared) {
            return;
        }
        float f3 = -0.6f;
        float f4 = 1.4f;
        if (pandaRenderState.isEating) {
            f3 -= 0.2f * Mth.sin(pandaRenderState.ageInTicks * 0.6f) + 0.2f;
            f4 -= 0.09f * Mth.sin(pandaRenderState.ageInTicks * 0.6f);
        }
        poseStack.pushPose();
        poseStack.translate(0.1f, f4, f3);
        itemStackRenderState.render(poseStack, multiBufferSource, n, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}

