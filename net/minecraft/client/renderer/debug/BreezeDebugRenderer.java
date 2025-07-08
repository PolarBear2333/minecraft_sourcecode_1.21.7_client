/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.network.protocol.common.custom.BreezeDebugPayload;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class BreezeDebugRenderer {
    private static final int JUMP_TARGET_LINE_COLOR = ARGB.color(255, 255, 100, 255);
    private static final int TARGET_LINE_COLOR = ARGB.color(255, 100, 255, 255);
    private static final int INNER_CIRCLE_COLOR = ARGB.color(255, 0, 255, 0);
    private static final int MIDDLE_CIRCLE_COLOR = ARGB.color(255, 255, 165, 0);
    private static final int OUTER_CIRCLE_COLOR = ARGB.color(255, 255, 0, 0);
    private static final int CIRCLE_VERTICES = 20;
    private static final float SEGMENT_SIZE_RADIANS = 0.31415927f;
    private final Minecraft minecraft;
    private final Map<Integer, BreezeDebugPayload.BreezeInfo> perEntity = new HashMap<Integer, BreezeDebugPayload.BreezeInfo>();

    public BreezeDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        LocalPlayer localPlayer = this.minecraft.player;
        localPlayer.level().getEntities(EntityType.BREEZE, localPlayer.getBoundingBox().inflate(100.0), breeze -> true).forEach(breeze -> {
            Optional<BreezeDebugPayload.BreezeInfo> optional = Optional.ofNullable(this.perEntity.get(breeze.getId()));
            optional.map(BreezeDebugPayload.BreezeInfo::attackTarget).map(n -> localPlayer.level().getEntity((int)n)).map(entity -> entity.getPosition(this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true))).ifPresent(vec3 -> {
                BreezeDebugRenderer.drawLine(poseStack, multiBufferSource, d, d2, d3, breeze.position(), vec3, TARGET_LINE_COLOR);
                Vec3 vec32 = vec3.add(0.0, 0.01f, 0.0);
                BreezeDebugRenderer.drawCircle(poseStack.last().pose(), d, d2, d3, multiBufferSource.getBuffer(RenderType.debugLineStrip(2.0)), vec32, 4.0f, INNER_CIRCLE_COLOR);
                BreezeDebugRenderer.drawCircle(poseStack.last().pose(), d, d2, d3, multiBufferSource.getBuffer(RenderType.debugLineStrip(2.0)), vec32, 8.0f, MIDDLE_CIRCLE_COLOR);
                BreezeDebugRenderer.drawCircle(poseStack.last().pose(), d, d2, d3, multiBufferSource.getBuffer(RenderType.debugLineStrip(2.0)), vec32, 24.0f, OUTER_CIRCLE_COLOR);
            });
            optional.map(BreezeDebugPayload.BreezeInfo::jumpTarget).ifPresent(blockPos -> {
                BreezeDebugRenderer.drawLine(poseStack, multiBufferSource, d, d2, d3, breeze.position(), blockPos.getCenter(), JUMP_TARGET_LINE_COLOR);
                DebugRenderer.renderFilledBox(poseStack, multiBufferSource, AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(blockPos)).move(-d, -d2, -d3), 1.0f, 0.0f, 0.0f, 1.0f);
            });
        });
    }

    private static void drawLine(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3, Vec3 vec3, Vec3 vec32, int n) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugLineStrip(2.0));
        vertexConsumer.addVertex(poseStack.last(), (float)(vec3.x - d), (float)(vec3.y - d2), (float)(vec3.z - d3)).setColor(n);
        vertexConsumer.addVertex(poseStack.last(), (float)(vec32.x - d), (float)(vec32.y - d2), (float)(vec32.z - d3)).setColor(n);
    }

    private static void drawCircle(Matrix4f matrix4f, double d, double d2, double d3, VertexConsumer vertexConsumer, Vec3 vec3, float f, int n) {
        for (int i = 0; i < 20; ++i) {
            BreezeDebugRenderer.drawCircleVertex(i, matrix4f, d, d2, d3, vertexConsumer, vec3, f, n);
        }
        BreezeDebugRenderer.drawCircleVertex(0, matrix4f, d, d2, d3, vertexConsumer, vec3, f, n);
    }

    private static void drawCircleVertex(int n, Matrix4f matrix4f, double d, double d2, double d3, VertexConsumer vertexConsumer, Vec3 vec3, float f, int n2) {
        float f2 = (float)n * 0.31415927f;
        Vec3 vec32 = vec3.add((double)f * Math.cos(f2), 0.0, (double)f * Math.sin(f2));
        vertexConsumer.addVertex(matrix4f, (float)(vec32.x - d), (float)(vec32.y - d2), (float)(vec32.z - d3)).setColor(n2);
    }

    public void clear() {
        this.perEntity.clear();
    }

    public void add(BreezeDebugPayload.BreezeInfo breezeInfo) {
        this.perEntity.put(breezeInfo.id(), breezeInfo);
    }
}

