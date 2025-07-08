/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;

public class GameTestDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private static final float PADDING = 0.02f;
    private final Map<BlockPos, Marker> markers = Maps.newHashMap();

    public void addMarker(BlockPos blockPos, int n, String string, int n2) {
        this.markers.put(blockPos, new Marker(n, string, Util.getMillis() + (long)n2));
    }

    @Override
    public void clear() {
        this.markers.clear();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        long l = Util.getMillis();
        this.markers.entrySet().removeIf(entry -> l > ((Marker)entry.getValue()).removeAtTime);
        this.markers.forEach((blockPos, marker) -> this.renderMarker(poseStack, multiBufferSource, (BlockPos)blockPos, (Marker)marker));
    }

    private void renderMarker(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos, Marker marker) {
        DebugRenderer.renderFilledBox(poseStack, multiBufferSource, blockPos, 0.02f, marker.getR(), marker.getG(), marker.getB(), marker.getA() * 0.75f);
        if (!marker.text.isEmpty()) {
            double d = (double)blockPos.getX() + 0.5;
            double d2 = (double)blockPos.getY() + 1.2;
            double d3 = (double)blockPos.getZ() + 0.5;
            DebugRenderer.renderFloatingText(poseStack, multiBufferSource, marker.text, d, d2, d3, -1, 0.01f, true, 0.0f, true);
        }
    }

    static class Marker {
        public int color;
        public String text;
        public long removeAtTime;

        public Marker(int n, String string, long l) {
            this.color = n;
            this.text = string;
            this.removeAtTime = l;
        }

        public float getR() {
            return (float)(this.color >> 16 & 0xFF) / 255.0f;
        }

        public float getG() {
            return (float)(this.color >> 8 & 0xFF) / 255.0f;
        }

        public float getB() {
            return (float)(this.color & 0xFF) / 255.0f;
        }

        public float getA() {
            return (float)(this.color >> 24 & 0xFF) / 255.0f;
        }
    }
}

