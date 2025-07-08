/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

public class SolidFaceRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;

    public SolidFaceRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        Matrix4f matrix4f = poseStack.last().pose();
        Level level = this.minecraft.player.level();
        BlockPos blockPos = BlockPos.containing(d, d2, d3);
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-6, -6, -6), blockPos.offset(6, 6, 6))) {
            BlockState blockState = level.getBlockState(blockPos2);
            if (blockState.is(Blocks.AIR)) continue;
            VoxelShape voxelShape = blockState.getShape(level, blockPos2);
            for (AABB aABB : voxelShape.toAabbs()) {
                VertexConsumer vertexConsumer;
                AABB aABB2 = aABB.move(blockPos2).inflate(0.002);
                float f = (float)(aABB2.minX - d);
                float f2 = (float)(aABB2.minY - d2);
                float f3 = (float)(aABB2.minZ - d3);
                float f4 = (float)(aABB2.maxX - d);
                float f5 = (float)(aABB2.maxY - d2);
                float f6 = (float)(aABB2.maxZ - d3);
                int n = -2130771968;
                if (blockState.isFaceSturdy(level, blockPos2, Direction.WEST)) {
                    vertexConsumer = multiBufferSource.getBuffer(RenderType.debugFilledBox());
                    vertexConsumer.addVertex(matrix4f, f, f2, f3).setColor(-2130771968);
                    vertexConsumer.addVertex(matrix4f, f, f2, f6).setColor(-2130771968);
                    vertexConsumer.addVertex(matrix4f, f, f5, f3).setColor(-2130771968);
                    vertexConsumer.addVertex(matrix4f, f, f5, f6).setColor(-2130771968);
                }
                if (blockState.isFaceSturdy(level, blockPos2, Direction.SOUTH)) {
                    vertexConsumer = multiBufferSource.getBuffer(RenderType.debugFilledBox());
                    vertexConsumer.addVertex(matrix4f, f, f5, f6).setColor(-2130771968);
                    vertexConsumer.addVertex(matrix4f, f, f2, f6).setColor(-2130771968);
                    vertexConsumer.addVertex(matrix4f, f4, f5, f6).setColor(-2130771968);
                    vertexConsumer.addVertex(matrix4f, f4, f2, f6).setColor(-2130771968);
                }
                if (blockState.isFaceSturdy(level, blockPos2, Direction.EAST)) {
                    vertexConsumer = multiBufferSource.getBuffer(RenderType.debugFilledBox());
                    vertexConsumer.addVertex(matrix4f, f4, f2, f6).setColor(-2130771968);
                    vertexConsumer.addVertex(matrix4f, f4, f2, f3).setColor(-2130771968);
                    vertexConsumer.addVertex(matrix4f, f4, f5, f6).setColor(-2130771968);
                    vertexConsumer.addVertex(matrix4f, f4, f5, f3).setColor(-2130771968);
                }
                if (blockState.isFaceSturdy(level, blockPos2, Direction.NORTH)) {
                    vertexConsumer = multiBufferSource.getBuffer(RenderType.debugFilledBox());
                    vertexConsumer.addVertex(matrix4f, f4, f5, f3).setColor(-2130771968);
                    vertexConsumer.addVertex(matrix4f, f4, f2, f3).setColor(-2130771968);
                    vertexConsumer.addVertex(matrix4f, f, f5, f3).setColor(-2130771968);
                    vertexConsumer.addVertex(matrix4f, f, f2, f3).setColor(-2130771968);
                }
                if (blockState.isFaceSturdy(level, blockPos2, Direction.DOWN)) {
                    vertexConsumer = multiBufferSource.getBuffer(RenderType.debugFilledBox());
                    vertexConsumer.addVertex(matrix4f, f, f2, f3).setColor(-2130771968);
                    vertexConsumer.addVertex(matrix4f, f4, f2, f3).setColor(-2130771968);
                    vertexConsumer.addVertex(matrix4f, f, f2, f6).setColor(-2130771968);
                    vertexConsumer.addVertex(matrix4f, f4, f2, f6).setColor(-2130771968);
                }
                if (!blockState.isFaceSturdy(level, blockPos2, Direction.UP)) continue;
                vertexConsumer = multiBufferSource.getBuffer(RenderType.debugFilledBox());
                vertexConsumer.addVertex(matrix4f, f, f5, f3).setColor(-2130771968);
                vertexConsumer.addVertex(matrix4f, f, f5, f6).setColor(-2130771968);
                vertexConsumer.addVertex(matrix4f, f4, f5, f3).setColor(-2130771968);
                vertexConsumer.addVertex(matrix4f, f4, f5, f6).setColor(-2130771968);
            }
        }
    }
}

