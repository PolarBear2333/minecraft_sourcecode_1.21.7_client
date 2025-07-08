/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BoundingBoxRenderable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public class BlockEntityWithBoundingBoxRenderer<T extends BlockEntity>
implements BlockEntityRenderer<T> {
    public BlockEntityWithBoundingBoxRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T t, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Vec3 vec3) {
        if (!Minecraft.getInstance().player.canUseGameMasterBlocks() && !Minecraft.getInstance().player.isSpectator()) {
            return;
        }
        BoundingBoxRenderable.Mode mode = ((BoundingBoxRenderable)t).renderMode();
        if (mode == BoundingBoxRenderable.Mode.NONE) {
            return;
        }
        BoundingBoxRenderable.RenderableBox renderableBox = ((BoundingBoxRenderable)t).getRenderableBox();
        BlockPos blockPos = renderableBox.localPos();
        Vec3i vec3i = renderableBox.size();
        if (vec3i.getX() < 1 || vec3i.getY() < 1 || vec3i.getZ() < 1) {
            return;
        }
        float f2 = 1.0f;
        float f3 = 0.9f;
        float f4 = 0.5f;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
        BlockPos blockPos2 = blockPos.offset(vec3i);
        ShapeRenderer.renderLineBox(poseStack, vertexConsumer, blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos2.getX(), blockPos2.getY(), blockPos2.getZ(), 0.9f, 0.9f, 0.9f, 1.0f, 0.5f, 0.5f, 0.5f);
        if (mode == BoundingBoxRenderable.Mode.BOX_AND_INVISIBLE_BLOCKS && ((BlockEntity)t).getLevel() != null) {
            this.renderInvisibleBlocks(t, ((BlockEntity)t).getLevel(), blockPos, vec3i, multiBufferSource, poseStack);
        }
    }

    private void renderInvisibleBlocks(T t, BlockGetter blockGetter, BlockPos blockPos, Vec3i vec3i, MultiBufferSource multiBufferSource, PoseStack poseStack) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
        BlockPos blockPos2 = ((BlockEntity)t).getBlockPos();
        BlockPos blockPos3 = blockPos2.offset(blockPos);
        for (BlockPos blockPos4 : BlockPos.betweenClosed(blockPos3, blockPos3.offset(vec3i).offset(-1, -1, -1))) {
            boolean bl;
            BlockState blockState = blockGetter.getBlockState(blockPos4);
            boolean bl2 = blockState.isAir();
            boolean bl3 = blockState.is(Blocks.STRUCTURE_VOID);
            boolean bl4 = blockState.is(Blocks.BARRIER);
            boolean bl5 = blockState.is(Blocks.LIGHT);
            boolean bl6 = bl = bl3 || bl4 || bl5;
            if (!bl2 && !bl) continue;
            float f = bl2 ? 0.05f : 0.0f;
            double d = (float)(blockPos4.getX() - blockPos2.getX()) + 0.45f - f;
            double d2 = (float)(blockPos4.getY() - blockPos2.getY()) + 0.45f - f;
            double d3 = (float)(blockPos4.getZ() - blockPos2.getZ()) + 0.45f - f;
            double d4 = (float)(blockPos4.getX() - blockPos2.getX()) + 0.55f + f;
            double d5 = (float)(blockPos4.getY() - blockPos2.getY()) + 0.55f + f;
            double d6 = (float)(blockPos4.getZ() - blockPos2.getZ()) + 0.55f + f;
            if (bl2) {
                ShapeRenderer.renderLineBox(poseStack, vertexConsumer, d, d2, d3, d4, d5, d6, 0.5f, 0.5f, 1.0f, 1.0f, 0.5f, 0.5f, 1.0f);
                continue;
            }
            if (bl3) {
                ShapeRenderer.renderLineBox(poseStack, vertexConsumer, d, d2, d3, d4, d5, d6, 1.0f, 0.75f, 0.75f, 1.0f, 1.0f, 0.75f, 0.75f);
                continue;
            }
            if (bl4) {
                ShapeRenderer.renderLineBox(poseStack, vertexConsumer, d, d2, d3, d4, d5, d6, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f);
                continue;
            }
            if (!bl5) continue;
            ShapeRenderer.renderLineBox(poseStack, vertexConsumer, d, d2, d3, d4, d5, d6, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f);
        }
    }

    private void renderStructureVoids(T t, BlockPos blockPos, Vec3i vec3i, VertexConsumer vertexConsumer, PoseStack poseStack) {
        Level level = ((BlockEntity)t).getLevel();
        if (level == null) {
            return;
        }
        BlockPos blockPos2 = ((BlockEntity)t).getBlockPos();
        BitSetDiscreteVoxelShape bitSetDiscreteVoxelShape = new BitSetDiscreteVoxelShape(vec3i.getX(), vec3i.getY(), vec3i.getZ());
        for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos, blockPos.offset(vec3i).offset(-1, -1, -1))) {
            if (!level.getBlockState(blockPos3).is(Blocks.STRUCTURE_VOID)) continue;
            ((DiscreteVoxelShape)bitSetDiscreteVoxelShape).fill(blockPos3.getX() - blockPos.getX(), blockPos3.getY() - blockPos.getY(), blockPos3.getZ() - blockPos.getZ());
        }
        bitSetDiscreteVoxelShape.forAllFaces((direction, n, n2, n3) -> {
            float f = 0.48f;
            float f2 = (float)(n + blockPos.getX() - blockPos2.getX()) + 0.5f - 0.48f;
            float f3 = (float)(n2 + blockPos.getY() - blockPos2.getY()) + 0.5f - 0.48f;
            float f4 = (float)(n3 + blockPos.getZ() - blockPos2.getZ()) + 0.5f - 0.48f;
            float f5 = (float)(n + blockPos.getX() - blockPos2.getX()) + 0.5f + 0.48f;
            float f6 = (float)(n2 + blockPos.getY() - blockPos2.getY()) + 0.5f + 0.48f;
            float f7 = (float)(n3 + blockPos.getZ() - blockPos2.getZ()) + 0.5f + 0.48f;
            ShapeRenderer.renderFace(poseStack, vertexConsumer, direction, f2, f3, f4, f5, f6, f7, 0.75f, 0.75f, 1.0f, 0.2f);
        });
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}

