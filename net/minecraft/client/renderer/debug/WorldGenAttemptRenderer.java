/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;

public class WorldGenAttemptRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final List<BlockPos> toRender = Lists.newArrayList();
    private final List<Float> scales = Lists.newArrayList();
    private final List<Float> alphas = Lists.newArrayList();
    private final List<Float> reds = Lists.newArrayList();
    private final List<Float> greens = Lists.newArrayList();
    private final List<Float> blues = Lists.newArrayList();

    public void addPos(BlockPos blockPos, float f, float f2, float f3, float f4, float f5) {
        this.toRender.add(blockPos);
        this.scales.add(Float.valueOf(f));
        this.alphas.add(Float.valueOf(f5));
        this.reds.add(Float.valueOf(f2));
        this.greens.add(Float.valueOf(f3));
        this.blues.add(Float.valueOf(f4));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugFilledBox());
        for (int i = 0; i < this.toRender.size(); ++i) {
            BlockPos blockPos = this.toRender.get(i);
            Float f = this.scales.get(i);
            float f2 = f.floatValue() / 2.0f;
            ShapeRenderer.addChainedFilledBoxVertices(poseStack, vertexConsumer, (double)((float)blockPos.getX() + 0.5f - f2) - d, (double)((float)blockPos.getY() + 0.5f - f2) - d2, (double)((float)blockPos.getZ() + 0.5f - f2) - d3, (double)((float)blockPos.getX() + 0.5f + f2) - d, (double)((float)blockPos.getY() + 0.5f + f2) - d2, (double)((float)blockPos.getZ() + 0.5f + f2) - d3, this.reds.get(i).floatValue(), this.greens.get(i).floatValue(), this.blues.get(i).floatValue(), this.alphas.get(i).floatValue());
        }
    }
}

