/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;

public final class MissingTextureAtlasSprite {
    private static final int MISSING_IMAGE_WIDTH = 16;
    private static final int MISSING_IMAGE_HEIGHT = 16;
    private static final String MISSING_TEXTURE_NAME = "missingno";
    private static final ResourceLocation MISSING_TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("missingno");

    public static NativeImage generateMissingImage() {
        return MissingTextureAtlasSprite.generateMissingImage(16, 16);
    }

    public static NativeImage generateMissingImage(int n, int n2) {
        NativeImage nativeImage = new NativeImage(n, n2, false);
        int n3 = -524040;
        for (int i = 0; i < n2; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i < n2 / 2 ^ j < n / 2) {
                    nativeImage.setPixel(j, i, -524040);
                    continue;
                }
                nativeImage.setPixel(j, i, -16777216);
            }
        }
        return nativeImage;
    }

    public static SpriteContents create() {
        NativeImage nativeImage = MissingTextureAtlasSprite.generateMissingImage(16, 16);
        return new SpriteContents(MISSING_TEXTURE_LOCATION, new FrameSize(16, 16), nativeImage, ResourceMetadata.EMPTY);
    }

    public static ResourceLocation getLocation() {
        return MISSING_TEXTURE_LOCATION;
    }
}

