/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.DolphinRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

public class DolphinCarryingItemLayer
extends RenderLayer<DolphinRenderState, DolphinModel> {
    public DolphinCarryingItemLayer(RenderLayerParent<DolphinRenderState, DolphinModel> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, DolphinRenderState dolphinRenderState, float f, float f2) {
        ItemStackRenderState itemStackRenderState = dolphinRenderState.heldItem;
        if (itemStackRenderState.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        float f3 = 1.0f;
        float f4 = -1.0f;
        float f5 = Mth.abs(dolphinRenderState.xRot) / 60.0f;
        if (dolphinRenderState.xRot < 0.0f) {
            poseStack.translate(0.0f, 1.0f - f5 * 0.5f, -1.0f + f5 * 0.5f);
        } else {
            poseStack.translate(0.0f, 1.0f + f5 * 0.8f, -1.0f + f5 * 0.2f);
        }
        itemStackRenderState.render(poseStack, multiBufferSource, n, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}

