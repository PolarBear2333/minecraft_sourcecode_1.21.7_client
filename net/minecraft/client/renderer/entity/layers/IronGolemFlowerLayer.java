/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;
import org.joml.Quaternionfc;

public class IronGolemFlowerLayer
extends RenderLayer<IronGolemRenderState, IronGolemModel> {
    private final BlockRenderDispatcher blockRenderer;

    public IronGolemFlowerLayer(RenderLayerParent<IronGolemRenderState, IronGolemModel> renderLayerParent, BlockRenderDispatcher blockRenderDispatcher) {
        super(renderLayerParent);
        this.blockRenderer = blockRenderDispatcher;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, IronGolemRenderState ironGolemRenderState, float f, float f2) {
        if (ironGolemRenderState.offerFlowerTick == 0) {
            return;
        }
        poseStack.pushPose();
        ModelPart modelPart = ((IronGolemModel)this.getParentModel()).getFlowerHoldingArm();
        modelPart.translateAndRotate(poseStack);
        poseStack.translate(-1.1875f, 1.0625f, -0.9375f);
        poseStack.translate(0.5f, 0.5f, 0.5f);
        float f3 = 0.5f;
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-90.0f));
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        this.blockRenderer.renderSingleBlock(Blocks.POPPY.defaultBlockState(), poseStack, multiBufferSource, n, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}

