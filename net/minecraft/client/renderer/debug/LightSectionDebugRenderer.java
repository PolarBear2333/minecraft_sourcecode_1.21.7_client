/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Matrix4f
 *  org.joml.Vector4f
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.time.Duration;
import java.time.Instant;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class LightSectionDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private static final Duration REFRESH_INTERVAL = Duration.ofMillis(500L);
    private static final int RADIUS = 10;
    private static final Vector4f LIGHT_AND_BLOCKS_COLOR = new Vector4f(1.0f, 1.0f, 0.0f, 0.25f);
    private static final Vector4f LIGHT_ONLY_COLOR = new Vector4f(0.25f, 0.125f, 0.0f, 0.125f);
    private final Minecraft minecraft;
    private final LightLayer lightLayer;
    private Instant lastUpdateTime = Instant.now();
    @Nullable
    private SectionData data;

    public LightSectionDebugRenderer(Minecraft minecraft, LightLayer lightLayer) {
        this.minecraft = minecraft;
        this.lightLayer = lightLayer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        Instant instant = Instant.now();
        if (this.data == null || Duration.between(this.lastUpdateTime, instant).compareTo(REFRESH_INTERVAL) > 0) {
            this.lastUpdateTime = instant;
            this.data = new SectionData(this.minecraft.level.getLightEngine(), SectionPos.of(this.minecraft.player.blockPosition()), 10, this.lightLayer);
        }
        LightSectionDebugRenderer.renderEdges(poseStack, this.data.lightAndBlocksShape, this.data.minPos, multiBufferSource, d, d2, d3, LIGHT_AND_BLOCKS_COLOR);
        LightSectionDebugRenderer.renderEdges(poseStack, this.data.lightShape, this.data.minPos, multiBufferSource, d, d2, d3, LIGHT_ONLY_COLOR);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugSectionQuads());
        LightSectionDebugRenderer.renderFaces(poseStack, this.data.lightAndBlocksShape, this.data.minPos, vertexConsumer, d, d2, d3, LIGHT_AND_BLOCKS_COLOR);
        LightSectionDebugRenderer.renderFaces(poseStack, this.data.lightShape, this.data.minPos, vertexConsumer, d, d2, d3, LIGHT_ONLY_COLOR);
    }

    private static void renderFaces(PoseStack poseStack, DiscreteVoxelShape discreteVoxelShape, SectionPos sectionPos, VertexConsumer vertexConsumer, double d, double d2, double d3, Vector4f vector4f) {
        discreteVoxelShape.forAllFaces((direction, n, n2, n3) -> {
            int n4 = n + sectionPos.getX();
            int n5 = n2 + sectionPos.getY();
            int n6 = n3 + sectionPos.getZ();
            LightSectionDebugRenderer.renderFace(poseStack, vertexConsumer, direction, d, d2, d3, n4, n5, n6, vector4f);
        });
    }

    private static void renderEdges(PoseStack poseStack, DiscreteVoxelShape discreteVoxelShape, SectionPos sectionPos, MultiBufferSource multiBufferSource, double d, double d2, double d3, Vector4f vector4f) {
        discreteVoxelShape.forAllEdges((n, n2, n3, n4, n5, n6) -> {
            int n7 = n + sectionPos.getX();
            int n8 = n2 + sectionPos.getY();
            int n9 = n3 + sectionPos.getZ();
            int n10 = n4 + sectionPos.getX();
            int n11 = n5 + sectionPos.getY();
            int n12 = n6 + sectionPos.getZ();
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugLineStrip(1.0));
            LightSectionDebugRenderer.renderEdge(poseStack, vertexConsumer, d, d2, d3, n7, n8, n9, n10, n11, n12, vector4f);
        }, true);
    }

    private static void renderFace(PoseStack poseStack, VertexConsumer vertexConsumer, Direction direction, double d, double d2, double d3, int n, int n2, int n3, Vector4f vector4f) {
        float f = (float)((double)SectionPos.sectionToBlockCoord(n) - d);
        float f2 = (float)((double)SectionPos.sectionToBlockCoord(n2) - d2);
        float f3 = (float)((double)SectionPos.sectionToBlockCoord(n3) - d3);
        ShapeRenderer.renderFace(poseStack, vertexConsumer, direction, f, f2, f3, f + 16.0f, f2 + 16.0f, f3 + 16.0f, vector4f.x(), vector4f.y(), vector4f.z(), vector4f.w());
    }

    private static void renderEdge(PoseStack poseStack, VertexConsumer vertexConsumer, double d, double d2, double d3, int n, int n2, int n3, int n4, int n5, int n6, Vector4f vector4f) {
        float f = (float)((double)SectionPos.sectionToBlockCoord(n) - d);
        float f2 = (float)((double)SectionPos.sectionToBlockCoord(n2) - d2);
        float f3 = (float)((double)SectionPos.sectionToBlockCoord(n3) - d3);
        float f4 = (float)((double)SectionPos.sectionToBlockCoord(n4) - d);
        float f5 = (float)((double)SectionPos.sectionToBlockCoord(n5) - d2);
        float f6 = (float)((double)SectionPos.sectionToBlockCoord(n6) - d3);
        Matrix4f matrix4f = poseStack.last().pose();
        vertexConsumer.addVertex(matrix4f, f, f2, f3).setColor(vector4f.x(), vector4f.y(), vector4f.z(), 1.0f);
        vertexConsumer.addVertex(matrix4f, f4, f5, f6).setColor(vector4f.x(), vector4f.y(), vector4f.z(), 1.0f);
    }

    static final class SectionData {
        final DiscreteVoxelShape lightAndBlocksShape;
        final DiscreteVoxelShape lightShape;
        final SectionPos minPos;

        SectionData(LevelLightEngine levelLightEngine, SectionPos sectionPos, int n, LightLayer lightLayer) {
            int n2 = n * 2 + 1;
            this.lightAndBlocksShape = new BitSetDiscreteVoxelShape(n2, n2, n2);
            this.lightShape = new BitSetDiscreteVoxelShape(n2, n2, n2);
            for (int i = 0; i < n2; ++i) {
                for (int j = 0; j < n2; ++j) {
                    for (int k = 0; k < n2; ++k) {
                        SectionPos sectionPos2 = SectionPos.of(sectionPos.x() + k - n, sectionPos.y() + j - n, sectionPos.z() + i - n);
                        LayerLightSectionStorage.SectionType sectionType = levelLightEngine.getDebugSectionType(lightLayer, sectionPos2);
                        if (sectionType == LayerLightSectionStorage.SectionType.LIGHT_AND_DATA) {
                            this.lightAndBlocksShape.fill(k, j, i);
                            this.lightShape.fill(k, j, i);
                            continue;
                        }
                        if (sectionType != LayerLightSectionStorage.SectionType.LIGHT_ONLY) continue;
                        this.lightShape.fill(k, j, i);
                    }
                }
            }
            this.minPos = SectionPos.of(sectionPos.x() - n, sectionPos.y() - n, sectionPos.z() - n);
        }
    }
}

