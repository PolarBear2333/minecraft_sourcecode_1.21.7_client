/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 */
package net.minecraft.client.gui.screens;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public class LevelLoadingScreen
extends Screen {
    private static final long NARRATION_DELAY_MS = 2000L;
    private final StoringChunkProgressListener progressListener;
    private long lastNarration = -1L;
    private boolean done;
    private static final Object2IntMap<ChunkStatus> COLORS = (Object2IntMap)Util.make(new Object2IntOpenHashMap(), object2IntOpenHashMap -> {
        object2IntOpenHashMap.defaultReturnValue(0);
        object2IntOpenHashMap.put((Object)ChunkStatus.EMPTY, 0x545454);
        object2IntOpenHashMap.put((Object)ChunkStatus.STRUCTURE_STARTS, 0x999999);
        object2IntOpenHashMap.put((Object)ChunkStatus.STRUCTURE_REFERENCES, 6250897);
        object2IntOpenHashMap.put((Object)ChunkStatus.BIOMES, 8434258);
        object2IntOpenHashMap.put((Object)ChunkStatus.NOISE, 0xD1D1D1);
        object2IntOpenHashMap.put((Object)ChunkStatus.SURFACE, 7497737);
        object2IntOpenHashMap.put((Object)ChunkStatus.CARVERS, 3159410);
        object2IntOpenHashMap.put((Object)ChunkStatus.FEATURES, 2213376);
        object2IntOpenHashMap.put((Object)ChunkStatus.INITIALIZE_LIGHT, 0xCCCCCC);
        object2IntOpenHashMap.put((Object)ChunkStatus.LIGHT, 16769184);
        object2IntOpenHashMap.put((Object)ChunkStatus.SPAWN, 15884384);
        object2IntOpenHashMap.put((Object)ChunkStatus.FULL, 0xFFFFFF);
    });

    public LevelLoadingScreen(StoringChunkProgressListener storingChunkProgressListener) {
        super(GameNarrator.NO_TITLE);
        this.progressListener = storingChunkProgressListener;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected boolean shouldNarrateNavigation() {
        return false;
    }

    @Override
    public void removed() {
        this.done = true;
        this.triggerImmediateNarration(true);
    }

    @Override
    protected void updateNarratedWidget(NarrationElementOutput narrationElementOutput) {
        if (this.done) {
            narrationElementOutput.add(NarratedElementType.TITLE, (Component)Component.translatable("narrator.loading.done"));
        } else {
            narrationElementOutput.add(NarratedElementType.TITLE, this.getFormattedProgress());
        }
    }

    private Component getFormattedProgress() {
        return Component.translatable("loading.progress", Mth.clamp(this.progressListener.getProgress(), 0, 100));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        long l = Util.getMillis();
        if (l - this.lastNarration > 2000L) {
            this.lastNarration = l;
            this.triggerImmediateNarration(true);
        }
        int n3 = this.width / 2;
        int n4 = this.height / 2;
        LevelLoadingScreen.renderChunks(guiGraphics, this.progressListener, n3, n4, 2, 0);
        int n5 = this.progressListener.getDiameter() + this.font.lineHeight + 2;
        guiGraphics.drawCenteredString(this.font, this.getFormattedProgress(), n3, n4 - n5, -1);
    }

    public static void renderChunks(GuiGraphics guiGraphics, StoringChunkProgressListener storingChunkProgressListener, int n, int n2, int n3, int n4) {
        int n5 = n3 + n4;
        int n6 = storingChunkProgressListener.getFullDiameter();
        int n7 = n6 * n5 - n4;
        int n8 = storingChunkProgressListener.getDiameter();
        int n9 = n8 * n5 - n4;
        int n10 = n - n9 / 2;
        int n11 = n2 - n9 / 2;
        int n12 = n7 / 2 + 1;
        int n13 = -16772609;
        if (n4 != 0) {
            guiGraphics.fill(n - n12, n2 - n12, n - n12 + 1, n2 + n12, -16772609);
            guiGraphics.fill(n + n12 - 1, n2 - n12, n + n12, n2 + n12, -16772609);
            guiGraphics.fill(n - n12, n2 - n12, n + n12, n2 - n12 + 1, -16772609);
            guiGraphics.fill(n - n12, n2 + n12 - 1, n + n12, n2 + n12, -16772609);
        }
        for (int i = 0; i < n8; ++i) {
            for (int j = 0; j < n8; ++j) {
                ChunkStatus chunkStatus = storingChunkProgressListener.getStatus(i, j);
                int n14 = n10 + i * n5;
                int n15 = n11 + j * n5;
                guiGraphics.fill(n14, n15, n14 + n3, n15 + n3, ARGB.opaque(COLORS.getInt((Object)chunkStatus)));
            }
        }
    }
}

