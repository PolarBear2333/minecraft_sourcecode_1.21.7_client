/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public class PanoramaRenderer {
    public static final ResourceLocation PANORAMA_OVERLAY = ResourceLocation.withDefaultNamespace("textures/gui/title/background/panorama_overlay.png");
    private final Minecraft minecraft;
    private final CubeMap cubeMap;
    private float spin;

    public PanoramaRenderer(CubeMap cubeMap) {
        this.cubeMap = cubeMap;
        this.minecraft = Minecraft.getInstance();
    }

    public void render(GuiGraphics guiGraphics, int n, int n2, boolean bl) {
        if (bl) {
            float f = this.minecraft.getDeltaTracker().getRealtimeDeltaTicks();
            float f2 = (float)((double)f * this.minecraft.options.panoramaSpeed().get());
            this.spin = PanoramaRenderer.wrap(this.spin + f2 * 0.1f, 360.0f);
        }
        this.cubeMap.render(this.minecraft, 10.0f, -this.spin);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, PANORAMA_OVERLAY, 0, 0, 0.0f, 0.0f, n, n2, 16, 128, 16, 128);
    }

    private static float wrap(float f, float f2) {
        return f > f2 ? f - f2 : f;
    }

    public void registerTextures(TextureManager textureManager) {
        this.cubeMap.registerTextures(textureManager);
    }
}

