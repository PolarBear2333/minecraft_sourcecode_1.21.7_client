/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Locale;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;

public class PathfindingRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Map<Integer, Path> pathMap = Maps.newHashMap();
    private final Map<Integer, Float> pathMaxDist = Maps.newHashMap();
    private final Map<Integer, Long> creationMap = Maps.newHashMap();
    private static final long TIMEOUT = 5000L;
    private static final float MAX_RENDER_DIST = 80.0f;
    private static final boolean SHOW_OPEN_CLOSED = true;
    private static final boolean SHOW_OPEN_CLOSED_COST_MALUS = false;
    private static final boolean SHOW_OPEN_CLOSED_NODE_TYPE_WITH_TEXT = false;
    private static final boolean SHOW_OPEN_CLOSED_NODE_TYPE_WITH_BOX = true;
    private static final boolean SHOW_GROUND_LABELS = true;
    private static final float TEXT_SCALE = 0.02f;

    public void addPath(int n, Path path, float f) {
        this.pathMap.put(n, path);
        this.creationMap.put(n, Util.getMillis());
        this.pathMaxDist.put(n, Float.valueOf(f));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        if (this.pathMap.isEmpty()) {
            return;
        }
        long l = Util.getMillis();
        for (Integer n : this.pathMap.keySet()) {
            Path path = this.pathMap.get(n);
            float f = this.pathMaxDist.get(n).floatValue();
            PathfindingRenderer.renderPath(poseStack, multiBufferSource, path, f, true, true, d, d2, d3);
        }
        for (Integer n : this.creationMap.keySet().toArray(new Integer[0])) {
            if (l - this.creationMap.get(n) <= 5000L) continue;
            this.pathMap.remove(n);
            this.creationMap.remove(n);
        }
    }

    public static void renderPath(PoseStack poseStack, MultiBufferSource multiBufferSource, Path path, float f, boolean bl, boolean bl2, double d, double d2, double d3) {
        PathfindingRenderer.renderPathLine(poseStack, multiBufferSource.getBuffer(RenderType.debugLineStrip(6.0)), path, d, d2, d3);
        BlockPos blockPos = path.getTarget();
        if (PathfindingRenderer.distanceToCamera(blockPos, d, d2, d3) <= 80.0f) {
            DebugRenderer.renderFilledBox(poseStack, multiBufferSource, new AABB((float)blockPos.getX() + 0.25f, (float)blockPos.getY() + 0.25f, (double)blockPos.getZ() + 0.25, (float)blockPos.getX() + 0.75f, (float)blockPos.getY() + 0.75f, (float)blockPos.getZ() + 0.75f).move(-d, -d2, -d3), 0.0f, 1.0f, 0.0f, 0.5f);
            for (int i = 0; i < path.getNodeCount(); ++i) {
                Node node = path.getNode(i);
                if (!(PathfindingRenderer.distanceToCamera(node.asBlockPos(), d, d2, d3) <= 80.0f)) continue;
                float f2 = i == path.getNextNodeIndex() ? 1.0f : 0.0f;
                float f3 = i == path.getNextNodeIndex() ? 0.0f : 1.0f;
                DebugRenderer.renderFilledBox(poseStack, multiBufferSource, new AABB((float)node.x + 0.5f - f, (float)node.y + 0.01f * (float)i, (float)node.z + 0.5f - f, (float)node.x + 0.5f + f, (float)node.y + 0.25f + 0.01f * (float)i, (float)node.z + 0.5f + f).move(-d, -d2, -d3), f2, 0.0f, f3, 0.5f);
            }
        }
        Path.DebugData debugData = path.debugData();
        if (bl && debugData != null) {
            for (Node node : debugData.closedSet()) {
                if (!(PathfindingRenderer.distanceToCamera(node.asBlockPos(), d, d2, d3) <= 80.0f)) continue;
                DebugRenderer.renderFilledBox(poseStack, multiBufferSource, new AABB((float)node.x + 0.5f - f / 2.0f, (float)node.y + 0.01f, (float)node.z + 0.5f - f / 2.0f, (float)node.x + 0.5f + f / 2.0f, (double)node.y + 0.1, (float)node.z + 0.5f + f / 2.0f).move(-d, -d2, -d3), 1.0f, 0.8f, 0.8f, 0.5f);
            }
            for (Node node : debugData.openSet()) {
                if (!(PathfindingRenderer.distanceToCamera(node.asBlockPos(), d, d2, d3) <= 80.0f)) continue;
                DebugRenderer.renderFilledBox(poseStack, multiBufferSource, new AABB((float)node.x + 0.5f - f / 2.0f, (float)node.y + 0.01f, (float)node.z + 0.5f - f / 2.0f, (float)node.x + 0.5f + f / 2.0f, (double)node.y + 0.1, (float)node.z + 0.5f + f / 2.0f).move(-d, -d2, -d3), 0.8f, 1.0f, 1.0f, 0.5f);
            }
        }
        if (bl2) {
            for (int i = 0; i < path.getNodeCount(); ++i) {
                Node node = path.getNode(i);
                if (!(PathfindingRenderer.distanceToCamera(node.asBlockPos(), d, d2, d3) <= 80.0f)) continue;
                DebugRenderer.renderFloatingText(poseStack, multiBufferSource, String.valueOf((Object)node.type), (double)node.x + 0.5, (double)node.y + 0.75, (double)node.z + 0.5, -1, 0.02f, true, 0.0f, true);
                DebugRenderer.renderFloatingText(poseStack, multiBufferSource, String.format(Locale.ROOT, "%.2f", Float.valueOf(node.costMalus)), (double)node.x + 0.5, (double)node.y + 0.25, (double)node.z + 0.5, -1, 0.02f, true, 0.0f, true);
            }
        }
    }

    public static void renderPathLine(PoseStack poseStack, VertexConsumer vertexConsumer, Path path, double d, double d2, double d3) {
        for (int i = 0; i < path.getNodeCount(); ++i) {
            Node node = path.getNode(i);
            if (PathfindingRenderer.distanceToCamera(node.asBlockPos(), d, d2, d3) > 80.0f) continue;
            float f = (float)i / (float)path.getNodeCount() * 0.33f;
            int n = i == 0 ? 0 : Mth.hsvToRgb(f, 0.9f, 0.9f);
            int n2 = n >> 16 & 0xFF;
            int n3 = n >> 8 & 0xFF;
            int n4 = n & 0xFF;
            vertexConsumer.addVertex(poseStack.last(), (float)((double)node.x - d + 0.5), (float)((double)node.y - d2 + 0.5), (float)((double)node.z - d3 + 0.5)).setColor(n2, n3, n4, 255);
        }
    }

    private static float distanceToCamera(BlockPos blockPos, double d, double d2, double d3) {
        return (float)(Math.abs((double)blockPos.getX() - d) + Math.abs((double)blockPos.getY() - d2) + Math.abs((double)blockPos.getZ() - d3));
    }
}

