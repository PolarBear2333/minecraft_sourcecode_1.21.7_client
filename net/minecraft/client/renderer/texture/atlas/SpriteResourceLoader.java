/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.texture.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

@FunctionalInterface
public interface SpriteResourceLoader {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static SpriteResourceLoader create(Collection<MetadataSectionType<?>> collection) {
        return (resourceLocation, resource) -> {
            FrameSize frameSize;
            NativeImage nativeImage;
            Object object;
            ResourceMetadata resourceMetadata;
            try {
                resourceMetadata = resource.metadata().copySections(collection);
            }
            catch (Exception exception) {
                LOGGER.error("Unable to parse metadata from {}", (Object)resourceLocation, (Object)exception);
                return null;
            }
            try {
                object = resource.open();
                try {
                    nativeImage = NativeImage.read((InputStream)object);
                }
                finally {
                    if (object != null) {
                        ((InputStream)object).close();
                    }
                }
            }
            catch (IOException iOException) {
                LOGGER.error("Using missing texture, unable to load {}", (Object)resourceLocation, (Object)iOException);
                return null;
            }
            object = resourceMetadata.getSection(AnimationMetadataSection.TYPE);
            if (((Optional)object).isPresent()) {
                frameSize = ((AnimationMetadataSection)((Optional)object).get()).calculateFrameSize(nativeImage.getWidth(), nativeImage.getHeight());
                if (!Mth.isMultipleOf(nativeImage.getWidth(), frameSize.width()) || !Mth.isMultipleOf(nativeImage.getHeight(), frameSize.height())) {
                    LOGGER.error("Image {} size {},{} is not multiple of frame size {},{}", new Object[]{resourceLocation, nativeImage.getWidth(), nativeImage.getHeight(), frameSize.width(), frameSize.height()});
                    nativeImage.close();
                    return null;
                }
            } else {
                frameSize = new FrameSize(nativeImage.getWidth(), nativeImage.getHeight());
            }
            return new SpriteContents(resourceLocation, frameSize, nativeImage, resourceMetadata);
        };
    }

    @Nullable
    public SpriteContents loadSprite(ResourceLocation var1, Resource var2);
}

