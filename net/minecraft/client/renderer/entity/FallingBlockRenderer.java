/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class FallingBlockRenderer
extends EntityRenderer<FallingBlockEntity, FallingBlockRenderState> {
    private final BlockRenderDispatcher dispatcher;

    public FallingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
        this.dispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public boolean shouldRender(FallingBlockEntity fallingBlockEntity, Frustum frustum, double d, double d2, double d3) {
        if (!super.shouldRender(fallingBlockEntity, frustum, d, d2, d3)) {
            return false;
        }
        return fallingBlockEntity.getBlockState() != fallingBlockEntity.level().getBlockState(fallingBlockEntity.blockPosition());
    }

    @Override
    public void render(FallingBlockRenderState fallingBlockRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        BlockState blockState = fallingBlockRenderState.blockState;
        if (blockState.getRenderShape() != RenderShape.MODEL) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(-0.5, 0.0, -0.5);
        List<BlockModelPart> list = this.dispatcher.getBlockModel(blockState).collectParts(RandomSource.create(blockState.getSeed(fallingBlockRenderState.startBlockPos)));
        this.dispatcher.getModelRenderer().tesselateBlock(fallingBlockRenderState, list, blockState, fallingBlockRenderState.blockPos, poseStack, multiBufferSource.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(blockState)), false, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(fallingBlockRenderState, poseStack, multiBufferSource, n);
    }

    @Override
    public FallingBlockRenderState createRenderState() {
        return new FallingBlockRenderState();
    }

    @Override
    public void extractRenderState(FallingBlockEntity fallingBlockEntity, FallingBlockRenderState fallingBlockRenderState, float f) {
        super.extractRenderState(fallingBlockEntity, fallingBlockRenderState, f);
        BlockPos blockPos = BlockPos.containing(fallingBlockEntity.getX(), fallingBlockEntity.getBoundingBox().maxY, fallingBlockEntity.getZ());
        fallingBlockRenderState.startBlockPos = fallingBlockEntity.getStartPos();
        fallingBlockRenderState.blockPos = blockPos;
        fallingBlockRenderState.blockState = fallingBlockEntity.getBlockState();
        fallingBlockRenderState.biome = fallingBlockEntity.level().getBiome(blockPos);
        fallingBlockRenderState.level = fallingBlockEntity.level();
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

