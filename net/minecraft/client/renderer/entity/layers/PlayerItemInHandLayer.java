/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

public class PlayerItemInHandLayer<S extends PlayerRenderState, M extends EntityModel<S> & HeadedModel>
extends ItemInHandLayer<S, M> {
    private static final float X_ROT_MIN = -0.5235988f;
    private static final float X_ROT_MAX = 1.5707964f;

    public PlayerItemInHandLayer(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    protected void renderArmWithItem(S s, ItemStackRenderState itemStackRenderState, HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        InteractionHand interactionHand;
        if (itemStackRenderState.isEmpty()) {
            return;
        }
        InteractionHand interactionHand2 = interactionHand = humanoidArm == ((PlayerRenderState)s).mainArm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        if (((PlayerRenderState)s).isUsingItem && ((PlayerRenderState)s).useItemHand == interactionHand && ((PlayerRenderState)s).attackTime < 1.0E-5f && !((PlayerRenderState)s).heldOnHead.isEmpty()) {
            this.renderItemHeldToEye(((PlayerRenderState)s).heldOnHead, humanoidArm, poseStack, multiBufferSource, n);
        } else {
            super.renderArmWithItem(s, itemStackRenderState, humanoidArm, poseStack, multiBufferSource, n);
        }
    }

    private void renderItemHeldToEye(ItemStackRenderState itemStackRenderState, HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        poseStack.pushPose();
        ((Model)this.getParentModel()).root().translateAndRotate(poseStack);
        ModelPart modelPart = ((HeadedModel)this.getParentModel()).getHead();
        float f = modelPart.xRot;
        modelPart.xRot = Mth.clamp(modelPart.xRot, -0.5235988f, 1.5707964f);
        modelPart.translateAndRotate(poseStack);
        modelPart.xRot = f;
        CustomHeadLayer.translateToHead(poseStack, CustomHeadLayer.Transforms.DEFAULT);
        boolean bl = humanoidArm == HumanoidArm.LEFT;
        poseStack.translate((bl ? -2.5f : 2.5f) / 16.0f, -0.0625f, 0.0f);
        itemStackRenderState.render(poseStack, multiBufferSource, n, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}

