/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleSupplier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.shapes.CollisionContext;

public class SupportBlockRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private double lastUpdateTime = Double.MIN_VALUE;
    private List<Entity> surroundEntities = Collections.emptyList();

    public SupportBlockRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        Entity entity;
        double d4 = Util.getNanos();
        if (d4 - this.lastUpdateTime > 1.0E8) {
            this.lastUpdateTime = d4;
            entity = this.minecraft.gameRenderer.getMainCamera().getEntity();
            this.surroundEntities = ImmutableList.copyOf(entity.level().getEntities(entity, entity.getBoundingBox().inflate(16.0)));
        }
        if ((entity = this.minecraft.player) != null && ((Player)entity).mainSupportingBlockPos.isPresent()) {
            this.drawHighlights(poseStack, multiBufferSource, d, d2, d3, entity, () -> 0.0, 1.0f, 0.0f, 0.0f);
        }
        for (Entity entity2 : this.surroundEntities) {
            if (entity2 == entity) continue;
            this.drawHighlights(poseStack, multiBufferSource, d, d2, d3, entity2, () -> this.getBias(entity2), 0.0f, 1.0f, 0.0f);
        }
    }

    private void drawHighlights(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3, Entity entity, DoubleSupplier doubleSupplier, float f, float f2, float f3) {
        entity.mainSupportingBlockPos.ifPresent(blockPos -> {
            double d4 = doubleSupplier.getAsDouble();
            BlockPos blockPos2 = entity.getOnPos();
            this.highlightPosition(blockPos2, poseStack, d, d2, d3, multiBufferSource, 0.02 + d4, f, f2, f3);
            BlockPos blockPos3 = entity.getOnPosLegacy();
            if (!blockPos3.equals(blockPos2)) {
                this.highlightPosition(blockPos3, poseStack, d, d2, d3, multiBufferSource, 0.04 + d4, 0.0f, 1.0f, 1.0f);
            }
        });
    }

    private double getBias(Entity entity) {
        return 0.02 * (double)(String.valueOf((double)entity.getId() + 0.132453657).hashCode() % 1000) / 1000.0;
    }

    private void highlightPosition(BlockPos blockPos, PoseStack poseStack, double d, double d2, double d3, MultiBufferSource multiBufferSource, double d4, float f, float f2, float f3) {
        double d5 = (double)blockPos.getX() - d - 2.0 * d4;
        double d6 = (double)blockPos.getY() - d2 - 2.0 * d4;
        double d7 = (double)blockPos.getZ() - d3 - 2.0 * d4;
        double d8 = d5 + 1.0 + 4.0 * d4;
        double d9 = d6 + 1.0 + 4.0 * d4;
        double d10 = d7 + 1.0 + 4.0 * d4;
        ShapeRenderer.renderLineBox(poseStack, multiBufferSource.getBuffer(RenderType.lines()), d5, d6, d7, d8, d9, d10, f, f2, f3, 0.4f);
        DebugRenderer.renderVoxelShape(poseStack, multiBufferSource.getBuffer(RenderType.lines()), this.minecraft.level.getBlockState(blockPos).getCollisionShape(this.minecraft.level, blockPos, CollisionContext.empty()).move(blockPos), -d, -d2, -d3, f, f2, f3, 1.0f, false);
    }
}

