/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

public class WaterDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;

    public WaterDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        FluidState fluidState;
        BlockPos blockPos = this.minecraft.player.blockPosition();
        Level level = this.minecraft.player.level();
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-10, -10, -10), blockPos.offset(10, 10, 10))) {
            fluidState = level.getFluidState(blockPos2);
            if (!fluidState.is(FluidTags.WATER)) continue;
            double d4 = (float)blockPos2.getY() + fluidState.getHeight(level, blockPos2);
            DebugRenderer.renderFilledBox(poseStack, multiBufferSource, new AABB((float)blockPos2.getX() + 0.01f, (float)blockPos2.getY() + 0.01f, (float)blockPos2.getZ() + 0.01f, (float)blockPos2.getX() + 0.99f, d4, (float)blockPos2.getZ() + 0.99f).move(-d, -d2, -d3), 0.0f, 1.0f, 0.0f, 0.15f);
        }
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-10, -10, -10), blockPos.offset(10, 10, 10))) {
            fluidState = level.getFluidState(blockPos2);
            if (!fluidState.is(FluidTags.WATER)) continue;
            DebugRenderer.renderFloatingText(poseStack, multiBufferSource, String.valueOf(fluidState.getAmount()), (double)blockPos2.getX() + 0.5, (float)blockPos2.getY() + fluidState.getHeight(level, blockPos2), (double)blockPos2.getZ() + 0.5, -16777216);
        }
    }
}

