/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.hash.Hashing
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public class FaviconTexture
implements AutoCloseable {
    private static final ResourceLocation MISSING_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/unknown_server.png");
    private static final int WIDTH = 64;
    private static final int HEIGHT = 64;
    private final TextureManager textureManager;
    private final ResourceLocation textureLocation;
    @Nullable
    private DynamicTexture texture;
    private boolean closed;

    private FaviconTexture(TextureManager textureManager, ResourceLocation resourceLocation) {
        this.textureManager = textureManager;
        this.textureLocation = resourceLocation;
    }

    public static FaviconTexture forWorld(TextureManager textureManager, String string) {
        return new FaviconTexture(textureManager, ResourceLocation.withDefaultNamespace("worlds/" + Util.sanitizeName(string, ResourceLocation::validPathChar) + "/" + String.valueOf(Hashing.sha1().hashUnencodedChars((CharSequence)string)) + "/icon"));
    }

    public static FaviconTexture forServer(TextureManager textureManager, String string) {
        return new FaviconTexture(textureManager, ResourceLocation.withDefaultNamespace("servers/" + String.valueOf(Hashing.sha1().hashUnencodedChars((CharSequence)string)) + "/icon"));
    }

    public void upload(NativeImage nativeImage) {
        if (nativeImage.getWidth() != 64 || nativeImage.getHeight() != 64) {
            nativeImage.close();
            throw new IllegalArgumentException("Icon must be 64x64, but was " + nativeImage.getWidth() + "x" + nativeImage.getHeight());
        }
        try {
            this.checkOpen();
            if (this.texture == null) {
                this.texture = new DynamicTexture(() -> "Favicon " + String.valueOf(this.textureLocation), nativeImage);
            } else {
                this.texture.setPixels(nativeImage);
                this.texture.upload();
            }
            this.textureManager.register(this.textureLocation, this.texture);
        }
        catch (Throwable throwable) {
            nativeImage.close();
            this.clear();
            throw throwable;
        }
    }

    public void clear() {
        this.checkOpen();
        if (this.texture != null) {
            this.textureManager.release(this.textureLocation);
            this.texture.close();
            this.texture = null;
        }
    }

    public ResourceLocation textureLocation() {
        return this.texture != null ? this.textureLocation : MISSING_LOCATION;
    }

    @Override
    public void close() {
        this.clear();
        this.closed = true;
    }

    private void checkOpen() {
        if (this.closed) {
            throw new IllegalStateException("Icon already closed");
        }
    }
}

