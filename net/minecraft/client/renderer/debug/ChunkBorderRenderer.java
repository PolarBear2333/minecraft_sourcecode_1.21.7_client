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
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.joml.Matrix4f;

public class ChunkBorderRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private static final int CELL_BORDER = ARGB.color(255, 0, 155, 155);
    private static final int YELLOW = ARGB.color(255, 255, 255, 0);

    public ChunkBorderRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        int n;
        int n2;
        Entity entity = this.minecraft.gameRenderer.getMainCamera().getEntity();
        float f = (float)((double)this.minecraft.level.getMinY() - d2);
        float f2 = (float)((double)(this.minecraft.level.getMaxY() + 1) - d2);
        ChunkPos chunkPos = entity.chunkPosition();
        float f3 = (float)((double)chunkPos.getMinBlockX() - d);
        float f4 = (float)((double)chunkPos.getMinBlockZ() - d3);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugLineStrip(1.0));
        Matrix4f matrix4f = poseStack.last().pose();
        for (n2 = -16; n2 <= 32; n2 += 16) {
            for (n = -16; n <= 32; n += 16) {
                vertexConsumer.addVertex(matrix4f, f3 + (float)n2, f, f4 + (float)n).setColor(1.0f, 0.0f, 0.0f, 0.0f);
                vertexConsumer.addVertex(matrix4f, f3 + (float)n2, f, f4 + (float)n).setColor(1.0f, 0.0f, 0.0f, 0.5f);
                vertexConsumer.addVertex(matrix4f, f3 + (float)n2, f2, f4 + (float)n).setColor(1.0f, 0.0f, 0.0f, 0.5f);
                vertexConsumer.addVertex(matrix4f, f3 + (float)n2, f2, f4 + (float)n).setColor(1.0f, 0.0f, 0.0f, 0.0f);
            }
        }
        for (n2 = 2; n2 < 16; n2 += 2) {
            n = n2 % 4 == 0 ? CELL_BORDER : YELLOW;
            vertexConsumer.addVertex(matrix4f, f3 + (float)n2, f, f4).setColor(1.0f, 1.0f, 0.0f, 0.0f);
            vertexConsumer.addVertex(matrix4f, f3 + (float)n2, f, f4).setColor(n);
            vertexConsumer.addVertex(matrix4f, f3 + (float)n2, f2, f4).setColor(n);
            vertexConsumer.addVertex(matrix4f, f3 + (float)n2, f2, f4).setColor(1.0f, 1.0f, 0.0f, 0.0f);
            vertexConsumer.addVertex(matrix4f, f3 + (float)n2, f, f4 + 16.0f).setColor(1.0f, 1.0f, 0.0f, 0.0f);
            vertexConsumer.addVertex(matrix4f, f3 + (float)n2, f, f4 + 16.0f).setColor(n);
            vertexConsumer.addVertex(matrix4f, f3 + (float)n2, f2, f4 + 16.0f).setColor(n);
            vertexConsumer.addVertex(matrix4f, f3 + (float)n2, f2, f4 + 16.0f).setColor(1.0f, 1.0f, 0.0f, 0.0f);
        }
        for (n2 = 2; n2 < 16; n2 += 2) {
            n = n2 % 4 == 0 ? CELL_BORDER : YELLOW;
            vertexConsumer.addVertex(matrix4f, f3, f, f4 + (float)n2).setColor(1.0f, 1.0f, 0.0f, 0.0f);
            vertexConsumer.addVertex(matrix4f, f3, f, f4 + (float)n2).setColor(n);
            vertexConsumer.addVertex(matrix4f, f3, f2, f4 + (float)n2).setColor(n);
            vertexConsumer.addVertex(matrix4f, f3, f2, f4 + (float)n2).setColor(1.0f, 1.0f, 0.0f, 0.0f);
            vertexConsumer.addVertex(matrix4f, f3 + 16.0f, f, f4 + (float)n2).setColor(1.0f, 1.0f, 0.0f, 0.0f);
            vertexConsumer.addVertex(matrix4f, f3 + 16.0f, f, f4 + (float)n2).setColor(n);
            vertexConsumer.addVertex(matrix4f, f3 + 16.0f, f2, f4 + (float)n2).setColor(n);
            vertexConsumer.addVertex(matrix4f, f3 + 16.0f, f2, f4 + (float)n2).setColor(1.0f, 1.0f, 0.0f, 0.0f);
        }
        for (n2 = this.minecraft.level.getMinY(); n2 <= this.minecraft.level.getMaxY() + 1; n2 += 2) {
            float f5 = (float)((double)n2 - d2);
            int n3 = n2 % 8 == 0 ? CELL_BORDER : YELLOW;
            vertexConsumer.addVertex(matrix4f, f3, f5, f4).setColor(1.0f, 1.0f, 0.0f, 0.0f);
            vertexConsumer.addVertex(matrix4f, f3, f5, f4).setColor(n3);
            vertexConsumer.addVertex(matrix4f, f3, f5, f4 + 16.0f).setColor(n3);
            vertexConsumer.addVertex(matrix4f, f3 + 16.0f, f5, f4 + 16.0f).setColor(n3);
            vertexConsumer.addVertex(matrix4f, f3 + 16.0f, f5, f4).setColor(n3);
            vertexConsumer.addVertex(matrix4f, f3, f5, f4).setColor(n3);
            vertexConsumer.addVertex(matrix4f, f3, f5, f4).setColor(1.0f, 1.0f, 0.0f, 0.0f);
        }
        vertexConsumer = multiBufferSource.getBuffer(RenderType.debugLineStrip(2.0));
        for (n2 = 0; n2 <= 16; n2 += 16) {
            for (int i = 0; i <= 16; i += 16) {
                vertexConsumer.addVertex(matrix4f, f3 + (float)n2, f, f4 + (float)i).setColor(0.25f, 0.25f, 1.0f, 0.0f);
                vertexConsumer.addVertex(matrix4f, f3 + (float)n2, f, f4 + (float)i).setColor(0.25f, 0.25f, 1.0f, 1.0f);
                vertexConsumer.addVertex(matrix4f, f3 + (float)n2, f2, f4 + (float)i).setColor(0.25f, 0.25f, 1.0f, 1.0f);
                vertexConsumer.addVertex(matrix4f, f3 + (float)n2, f2, f4 + (float)i).setColor(0.25f, 0.25f, 1.0f, 0.0f);
            }
        }
        for (n2 = this.minecraft.level.getMinY(); n2 <= this.minecraft.level.getMaxY() + 1; n2 += 16) {
            float f6 = (float)((double)n2 - d2);
            vertexConsumer.addVertex(matrix4f, f3, f6, f4).setColor(0.25f, 0.25f, 1.0f, 0.0f);
            vertexConsumer.addVertex(matrix4f, f3, f6, f4).setColor(0.25f, 0.25f, 1.0f, 1.0f);
            vertexConsumer.addVertex(matrix4f, f3, f6, f4 + 16.0f).setColor(0.25f, 0.25f, 1.0f, 1.0f);
            vertexConsumer.addVertex(matrix4f, f3 + 16.0f, f6, f4 + 16.0f).setColor(0.25f, 0.25f, 1.0f, 1.0f);
            vertexConsumer.addVertex(matrix4f, f3 + 16.0f, f6, f4).setColor(0.25f, 0.25f, 1.0f, 1.0f);
            vertexConsumer.addVertex(matrix4f, f3, f6, f4).setColor(0.25f, 0.25f, 1.0f, 1.0f);
            vertexConsumer.addVertex(matrix4f, f3, f6, f4).setColor(0.25f, 0.25f, 1.0f, 0.0f);
        }
    }
}

