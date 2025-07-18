/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.chunk;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

public enum ChunkSectionLayer {
    SOLID(RenderPipelines.SOLID, 0x400000, true, false),
    CUTOUT_MIPPED(RenderPipelines.CUTOUT_MIPPED, 0x400000, true, false),
    CUTOUT(RenderPipelines.CUTOUT, 786432, false, false),
    TRANSLUCENT(RenderPipelines.TRANSLUCENT, 786432, true, true),
    TRIPWIRE(RenderPipelines.TRIPWIRE, 1536, true, true);

    private final RenderPipeline pipeline;
    private final int bufferSize;
    private final boolean useMipmaps;
    private final boolean sortOnUpload;
    private final String label;

    private ChunkSectionLayer(RenderPipeline renderPipeline, int n2, boolean bl, boolean bl2) {
        this.pipeline = renderPipeline;
        this.bufferSize = n2;
        this.useMipmaps = bl;
        this.sortOnUpload = bl2;
        this.label = this.toString().toLowerCase(Locale.ROOT);
    }

    public RenderPipeline pipeline() {
        return this.pipeline;
    }

    public int bufferSize() {
        return this.bufferSize;
    }

    public String label() {
        return this.label;
    }

    public boolean sortOnUpload() {
        return this.sortOnUpload;
    }

    public GpuTextureView textureView() {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        AbstractTexture abstractTexture = textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS);
        abstractTexture.setUseMipmaps(this.useMipmaps);
        return abstractTexture.getTextureView();
    }

    public RenderTarget outputTarget() {
        Minecraft minecraft = Minecraft.getInstance();
        switch (this.ordinal()) {
            case 4: {
                RenderTarget renderTarget = minecraft.levelRenderer.getWeatherTarget();
                return renderTarget != null ? renderTarget : minecraft.getMainRenderTarget();
            }
            case 3: {
                RenderTarget renderTarget = minecraft.levelRenderer.getTranslucentTarget();
                return renderTarget != null ? renderTarget : minecraft.getMainRenderTarget();
            }
        }
        return minecraft.getMainRenderTarget();
    }
}

