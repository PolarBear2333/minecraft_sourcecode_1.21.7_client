/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.render;

import com.mojang.blaze3d.textures.GpuTextureView;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;

public record TextureSetup(@Nullable GpuTextureView texure0, @Nullable GpuTextureView texure1, @Nullable GpuTextureView texure2) {
    private static final TextureSetup NO_TEXTURE_SETUP = new TextureSetup(null, null, null);
    private static int sortKeySeed;

    public static TextureSetup singleTexture(GpuTextureView gpuTextureView) {
        return new TextureSetup(gpuTextureView, null, null);
    }

    public static TextureSetup singleTextureWithLightmap(GpuTextureView gpuTextureView) {
        return new TextureSetup(gpuTextureView, null, Minecraft.getInstance().gameRenderer.lightTexture().getTextureView());
    }

    public static TextureSetup doubleTexture(GpuTextureView gpuTextureView, GpuTextureView gpuTextureView2) {
        return new TextureSetup(gpuTextureView, gpuTextureView2, null);
    }

    public static TextureSetup noTexture() {
        return NO_TEXTURE_SETUP;
    }

    public int getSortKey() {
        return this.hashCode();
    }

    public static void updateSortKeySeed() {
        sortKeySeed = Math.round(100000.0f * (float)Math.random());
    }
}

