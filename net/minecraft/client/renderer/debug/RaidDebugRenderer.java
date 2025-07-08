/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;

public class RaidDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private static final int MAX_RENDER_DIST = 160;
    private static final float TEXT_SCALE = 0.04f;
    private final Minecraft minecraft;
    private Collection<BlockPos> raidCenters = Lists.newArrayList();

    public RaidDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void setRaidCenters(Collection<BlockPos> collection) {
        this.raidCenters = collection;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        BlockPos blockPos = this.getCamera().getBlockPosition();
        for (BlockPos blockPos2 : this.raidCenters) {
            if (!blockPos.closerThan(blockPos2, 160.0)) continue;
            RaidDebugRenderer.highlightRaidCenter(poseStack, multiBufferSource, blockPos2);
        }
    }

    private static void highlightRaidCenter(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos) {
        DebugRenderer.renderFilledUnitCube(poseStack, multiBufferSource, blockPos, 1.0f, 0.0f, 0.0f, 0.15f);
        RaidDebugRenderer.renderTextOverBlock(poseStack, multiBufferSource, "Raid center", blockPos, -65536);
    }

    private static void renderTextOverBlock(PoseStack poseStack, MultiBufferSource multiBufferSource, String string, BlockPos blockPos, int n) {
        double d = (double)blockPos.getX() + 0.5;
        double d2 = (double)blockPos.getY() + 1.3;
        double d3 = (double)blockPos.getZ() + 0.5;
        DebugRenderer.renderFloatingText(poseStack, multiBufferSource, string, d, d2, d3, n, 0.04f, true, 0.0f, true);
    }

    private Camera getCamera() {
        return this.minecraft.gameRenderer.getMainCamera();
    }
}

