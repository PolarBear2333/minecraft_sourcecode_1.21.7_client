/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionfc;

public class CarriedBlockLayer
extends RenderLayer<EndermanRenderState, EndermanModel<EndermanRenderState>> {
    private final BlockRenderDispatcher blockRenderer;

    public CarriedBlockLayer(RenderLayerParent<EndermanRenderState, EndermanModel<EndermanRenderState>> renderLayerParent, BlockRenderDispatcher blockRenderDispatcher) {
        super(renderLayerParent);
        this.blockRenderer = blockRenderDispatcher;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, EndermanRenderState endermanRenderState, float f, float f2) {
        BlockState blockState = endermanRenderState.carriedBlock;
        if (blockState == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.6875f, -0.75f);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(20.0f));
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(45.0f));
        poseStack.translate(0.25f, 0.1875f, 0.25f);
        float f3 = 0.5f;
        poseStack.scale(-0.5f, -0.5f, 0.5f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(90.0f));
        this.blockRenderer.renderSingleBlock(blockState, poseStack, multiBufferSource, n, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}

