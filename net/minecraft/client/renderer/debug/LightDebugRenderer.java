/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;

public class LightDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private static final int MAX_RENDER_DIST = 10;

    public LightDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        ClientLevel clientLevel = this.minecraft.level;
        BlockPos blockPos = BlockPos.containing(d, d2, d3);
        LongOpenHashSet longOpenHashSet = new LongOpenHashSet();
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-10, -10, -10), blockPos.offset(10, 10, 10))) {
            int n = clientLevel.getBrightness(LightLayer.SKY, blockPos2);
            float f = (float)(15 - n) / 15.0f * 0.5f + 0.16f;
            int n2 = Mth.hsvToRgb(f, 0.9f, 0.9f);
            long l = SectionPos.blockToSection(blockPos2.asLong());
            if (longOpenHashSet.add(l)) {
                DebugRenderer.renderFloatingText(poseStack, multiBufferSource, clientLevel.getChunkSource().getLightEngine().getDebugData(LightLayer.SKY, SectionPos.of(l)), SectionPos.sectionToBlockCoord(SectionPos.x(l), 8), SectionPos.sectionToBlockCoord(SectionPos.y(l), 8), SectionPos.sectionToBlockCoord(SectionPos.z(l), 8), -65536, 0.3f);
            }
            if (n == 15) continue;
            DebugRenderer.renderFloatingText(poseStack, multiBufferSource, String.valueOf(n), (double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.25, (double)blockPos2.getZ() + 0.5, n2);
        }
    }
}

