/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.SnowGolemModel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SnowGolemRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionfc;

public class SnowGolemHeadLayer
extends RenderLayer<SnowGolemRenderState, SnowGolemModel> {
    private final BlockRenderDispatcher blockRenderer;

    public SnowGolemHeadLayer(RenderLayerParent<SnowGolemRenderState, SnowGolemModel> renderLayerParent, BlockRenderDispatcher blockRenderDispatcher) {
        super(renderLayerParent);
        this.blockRenderer = blockRenderDispatcher;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, SnowGolemRenderState snowGolemRenderState, float f, float f2) {
        if (!snowGolemRenderState.hasPumpkin) {
            return;
        }
        if (snowGolemRenderState.isInvisible && !snowGolemRenderState.appearsGlowing) {
            return;
        }
        poseStack.pushPose();
        ((SnowGolemModel)this.getParentModel()).getHead().translateAndRotate(poseStack);
        float f3 = 0.625f;
        poseStack.translate(0.0f, -0.34375f, 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f));
        poseStack.scale(0.625f, -0.625f, -0.625f);
        BlockState blockState = Blocks.CARVED_PUMPKIN.defaultBlockState();
        BlockStateModel blockStateModel = this.blockRenderer.getBlockModel(blockState);
        int n2 = LivingEntityRenderer.getOverlayCoords(snowGolemRenderState, 0.0f);
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        VertexConsumer vertexConsumer = snowGolemRenderState.appearsGlowing && snowGolemRenderState.isInvisible ? multiBufferSource.getBuffer(RenderType.outline(TextureAtlas.LOCATION_BLOCKS)) : multiBufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(blockState));
        ModelBlockRenderer.renderModel(poseStack.last(), vertexConsumer, blockStateModel, 0.0f, 0.0f, 0.0f, n, n2);
        poseStack.popPose();
    }
}

