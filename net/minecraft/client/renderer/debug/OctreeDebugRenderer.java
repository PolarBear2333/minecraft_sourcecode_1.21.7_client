/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Octree;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.mutable.MutableInt;

public class OctreeDebugRenderer {
    private final Minecraft minecraft;

    public OctreeDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void render(PoseStack poseStack, Frustum frustum, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        Octree octree = this.minecraft.levelRenderer.getSectionOcclusionGraph().getOctree();
        MutableInt mutableInt = new MutableInt(0);
        octree.visitNodes((node, bl, n, bl2) -> this.renderNode(node, poseStack, multiBufferSource, d, d2, d3, n, bl, mutableInt, bl2), frustum, 32);
    }

    private void renderNode(Octree.Node node, PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3, int n, boolean bl, MutableInt mutableInt, boolean bl2) {
        AABB aABB = node.getAABB();
        double d4 = aABB.getXsize();
        long l = Math.round(d4 / 16.0);
        if (l == 1L) {
            mutableInt.add(1);
            double d5 = aABB.getCenter().x;
            double d6 = aABB.getCenter().y;
            double d7 = aABB.getCenter().z;
            int n2 = bl2 ? -16711936 : -1;
            DebugRenderer.renderFloatingText(poseStack, multiBufferSource, String.valueOf(mutableInt.getValue()), d5, d6, d7, n2, 0.3f);
        }
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
        long l2 = l + 5L;
        ShapeRenderer.renderLineBox(poseStack, vertexConsumer, aABB.deflate(0.1 * (double)n).move(-d, -d2, -d3), OctreeDebugRenderer.getColorComponent(l2, 0.3f), OctreeDebugRenderer.getColorComponent(l2, 0.8f), OctreeDebugRenderer.getColorComponent(l2, 0.5f), bl ? 0.4f : 1.0f);
    }

    private static float getColorComponent(long l, float f) {
        float f2 = 0.1f;
        return Mth.frac(f * (float)l) * 0.9f + 0.1f;
    }
}

