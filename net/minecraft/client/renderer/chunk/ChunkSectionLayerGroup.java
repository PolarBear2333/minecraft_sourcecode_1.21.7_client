/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.chunk;

import com.mojang.blaze3d.pipeline.RenderTarget;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public enum ChunkSectionLayerGroup {
    OPAQUE(ChunkSectionLayer.SOLID, ChunkSectionLayer.CUTOUT_MIPPED, ChunkSectionLayer.CUTOUT),
    TRANSLUCENT(ChunkSectionLayer.TRANSLUCENT),
    TRIPWIRE(ChunkSectionLayer.TRIPWIRE);

    private final String label;
    private final ChunkSectionLayer[] layers;

    private ChunkSectionLayerGroup(ChunkSectionLayer ... chunkSectionLayerArray) {
        this.layers = chunkSectionLayerArray;
        this.label = this.toString().toLowerCase(Locale.ROOT);
    }

    public String label() {
        return this.label;
    }

    public ChunkSectionLayer[] layers() {
        return this.layers;
    }

    public RenderTarget outputTarget() {
        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget renderTarget = switch (this.ordinal()) {
            case 2 -> minecraft.levelRenderer.getWeatherTarget();
            case 1 -> minecraft.levelRenderer.getTranslucentTarget();
            default -> minecraft.getMainRenderTarget();
        };
        return renderTarget != null ? renderTarget : minecraft.getMainRenderTarget();
    }
}

