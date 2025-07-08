/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import org.joml.Quaternionfc;

public class WitchItemLayer
extends CrossedArmsItemLayer<WitchRenderState, WitchModel> {
    public WitchItemLayer(RenderLayerParent<WitchRenderState, WitchModel> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    protected void applyTranslation(WitchRenderState witchRenderState, PoseStack poseStack) {
        if (witchRenderState.isHoldingPotion) {
            ((WitchModel)this.getParentModel()).root().translateAndRotate(poseStack);
            ((WitchModel)this.getParentModel()).getHead().translateAndRotate(poseStack);
            ((WitchModel)this.getParentModel()).getNose().translateAndRotate(poseStack);
            poseStack.translate(0.0625f, 0.25f, 0.0f);
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(180.0f));
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(140.0f));
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(10.0f));
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(180.0f));
            return;
        }
        super.applyTranslation(witchRenderState, poseStack);
    }
}

