/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ARGB;

public class OverlayTexture
implements AutoCloseable {
    private static final int SIZE = 16;
    public static final int NO_WHITE_U = 0;
    public static final int RED_OVERLAY_V = 3;
    public static final int WHITE_OVERLAY_V = 10;
    public static final int NO_OVERLAY = OverlayTexture.pack(0, 10);
    private final DynamicTexture texture = new DynamicTexture("Entity Color Overlay", 16, 16, false);

    public OverlayTexture() {
        NativeImage nativeImage = this.texture.getPixels();
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                if (i < 8) {
                    nativeImage.setPixel(j, i, -1291911168);
                    continue;
                }
                int n = (int)((1.0f - (float)j / 15.0f * 0.75f) * 255.0f);
                nativeImage.setPixel(j, i, ARGB.color(n, -1));
            }
        }
        this.texture.setClamp(true);
        this.texture.upload();
    }

    @Override
    public void close() {
        this.texture.close();
    }

    public void setupOverlayColor() {
        RenderSystem.setupOverlayColor(this.texture.getTextureView());
    }

    public static int u(float f) {
        return (int)(f * 15.0f);
    }

    public static int v(boolean bl) {
        return bl ? 3 : 10;
    }

    public static int pack(int n, int n2) {
        return n | n2 << 16;
    }

    public static int pack(float f, boolean bl) {
        return OverlayTexture.pack(OverlayTexture.u(f), OverlayTexture.v(bl));
    }

    public void teardownOverlayColor() {
        RenderSystem.teardownOverlayColor();
    }
}

