/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public record TextureContents(NativeImage image, @Nullable TextureMetadataSection metadata) implements Closeable
{
    public static TextureContents load(ResourceManager resourceManager, ResourceLocation resourceLocation) throws IOException {
        NativeImage nativeImage;
        Resource resource = resourceManager.getResourceOrThrow(resourceLocation);
        try (Object object = resource.open();){
            nativeImage = NativeImage.read((InputStream)object);
        }
        object = resource.metadata().getSection(TextureMetadataSection.TYPE).orElse(null);
        return new TextureContents(nativeImage, (TextureMetadataSection)object);
    }

    public static TextureContents createMissing() {
        return new TextureContents(MissingTextureAtlasSprite.generateMissingImage(), null);
    }

    public boolean blur() {
        return this.metadata != null ? this.metadata.blur() : false;
    }

    public boolean clamp() {
        return this.metadata != null ? this.metadata.clamp() : false;
    }

    @Override
    public void close() {
        this.image.close();
    }
}

