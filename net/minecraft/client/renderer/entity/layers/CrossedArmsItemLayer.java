/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.VillagerLikeModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Quaternionfc;

public class CrossedArmsItemLayer<S extends HoldingEntityRenderState, M extends EntityModel<S>>
extends RenderLayer<S, M> {
    public CrossedArmsItemLayer(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, S s, float f, float f2) {
        ItemStackRenderState itemStackRenderState = ((HoldingEntityRenderState)s).heldItem;
        if (itemStackRenderState.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        this.applyTranslation(s, poseStack);
        itemStackRenderState.render(poseStack, multiBufferSource, n, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    protected void applyTranslation(S s, PoseStack poseStack) {
        ((VillagerLikeModel)this.getParentModel()).translateToArms(poseStack);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotation(0.75f));
        poseStack.scale(1.07f, 1.07f, 1.07f);
        poseStack.translate(0.0f, 0.13f, -0.34f);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotation((float)Math.PI));
    }
}

