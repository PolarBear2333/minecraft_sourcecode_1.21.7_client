/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Collections;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CollisionBoxRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private double lastUpdateTime = Double.MIN_VALUE;
    private List<VoxelShape> shapes = Collections.emptyList();

    public CollisionBoxRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        Object object;
        double d4 = Util.getNanos();
        if (d4 - this.lastUpdateTime > 1.0E8) {
            this.lastUpdateTime = d4;
            object = this.minecraft.gameRenderer.getMainCamera().getEntity();
            this.shapes = ImmutableList.copyOf(((Entity)object).level().getCollisions((Entity)object, ((Entity)object).getBoundingBox().inflate(6.0)));
        }
        object = multiBufferSource.getBuffer(RenderType.lines());
        for (VoxelShape voxelShape : this.shapes) {
            DebugRenderer.renderVoxelShape(poseStack, (VertexConsumer)object, voxelShape, -d, -d2, -d3, 1.0f, 1.0f, 1.0f, 1.0f, true);
        }
    }
}

