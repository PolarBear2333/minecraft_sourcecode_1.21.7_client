/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.TextureFormat;
import java.io.IOException;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class CubeMapTexture
extends ReloadableTexture {
    private static final String[] SUFFIXES = new String[]{"_1.png", "_3.png", "_5.png", "_4.png", "_0.png", "_2.png"};

    public CubeMapTexture(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override
    public TextureContents loadContents(ResourceManager resourceManager) throws IOException {
        ResourceLocation resourceLocation = this.resourceId();
        try (TextureContents textureContents = TextureContents.load(resourceManager, resourceLocation.withSuffix(SUFFIXES[0]));){
            int n = textureContents.image().getWidth();
            int n2 = textureContents.image().getHeight();
            NativeImage nativeImage = new NativeImage(n, n2 * 6, false);
            textureContents.image().copyRect(nativeImage, 0, 0, 0, 0, n, n2, false, true);
            for (int i = 1; i < 6; ++i) {
                try (TextureContents textureContents2 = TextureContents.load(resourceManager, resourceLocation.withSuffix(SUFFIXES[i]));){
                    if (textureContents2.image().getWidth() != n || textureContents2.image().getHeight() != n2) {
                        throw new IOException("Image dimensions of cubemap '" + String.valueOf(resourceLocation) + "' sides do not match: part 0 is " + n + "x" + n2 + ", but part " + i + " is " + textureContents2.image().getWidth() + "x" + textureContents2.image().getHeight());
                    }
                    textureContents2.image().copyRect(nativeImage, 0, 0, 0, i * n2, n, n2, false, true);
                    continue;
                }
            }
            TextureContents textureContents3 = new TextureContents(nativeImage, new TextureMetadataSection(true, false));
            return textureContents3;
        }
    }

    @Override
    protected void doLoad(NativeImage nativeImage, boolean bl, boolean bl2) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        int n = nativeImage.getWidth();
        int n2 = nativeImage.getHeight() / 6;
        this.close();
        this.texture = gpuDevice.createTexture(this.resourceId()::toString, 21, TextureFormat.RGBA8, n, n2, 6, 1);
        this.textureView = gpuDevice.createTextureView(this.texture);
        this.setFilter(bl, false);
        this.setClamp(bl2);
        for (int i = 0; i < 6; ++i) {
            gpuDevice.createCommandEncoder().writeToTexture(this.texture, nativeImage, 0, i, 0, 0, n, n2, 0, n2 * i);
        }
    }
}

