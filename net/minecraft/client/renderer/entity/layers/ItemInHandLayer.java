/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Quaternionfc;

public class ItemInHandLayer<S extends ArmedEntityRenderState, M extends EntityModel<S>>
extends RenderLayer<S, M> {
    public ItemInHandLayer(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, S s, float f, float f2) {
        this.renderArmWithItem(s, ((ArmedEntityRenderState)s).rightHandItem, HumanoidArm.RIGHT, poseStack, multiBufferSource, n);
        this.renderArmWithItem(s, ((ArmedEntityRenderState)s).leftHandItem, HumanoidArm.LEFT, poseStack, multiBufferSource, n);
    }

    protected void renderArmWithItem(S s, ItemStackRenderState itemStackRenderState, HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        if (itemStackRenderState.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        ((ArmedModel)this.getParentModel()).translateToHand(humanoidArm, poseStack);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-90.0f));
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f));
        boolean bl = humanoidArm == HumanoidArm.LEFT;
        poseStack.translate((float)(bl ? -1 : 1) / 16.0f, 0.125f, -0.625f);
        itemStackRenderState.render(poseStack, multiBufferSource, n, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}

