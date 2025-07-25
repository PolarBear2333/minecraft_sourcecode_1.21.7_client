/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import javax.annotation.Nullable;

public abstract class AbstractTexture
implements AutoCloseable {
    @Nullable
    protected GpuTexture texture;
    @Nullable
    protected GpuTextureView textureView;

    public void setClamp(boolean bl) {
        if (this.texture == null) {
            throw new IllegalStateException("Texture does not exist, can't change its clamp before something initializes it");
        }
        this.texture.setAddressMode(bl ? AddressMode.CLAMP_TO_EDGE : AddressMode.REPEAT);
    }

    public void setFilter(boolean bl, boolean bl2) {
        if (this.texture == null) {
            throw new IllegalStateException("Texture does not exist, can't get change its filter before something initializes it");
        }
        this.texture.setTextureFilter(bl ? FilterMode.LINEAR : FilterMode.NEAREST, bl2);
    }

    public void setUseMipmaps(boolean bl) {
        if (this.texture == null) {
            throw new IllegalStateException("Texture does not exist, can't get change its filter before something initializes it");
        }
        this.texture.setUseMipmaps(bl);
    }

    @Override
    public void close() {
        if (this.texture != null) {
            this.texture.close();
            this.texture = null;
        }
        if (this.textureView != null) {
            this.textureView.close();
            this.textureView = null;
        }
    }

    public GpuTexture getTexture() {
        if (this.texture == null) {
            throw new IllegalStateException("Texture does not exist, can't get it before something initializes it");
        }
        return this.texture;
    }

    public GpuTextureView getTextureView() {
        if (this.textureView == null) {
            throw new IllegalStateException("Texture view does not exist, can't get it before something initializes it");
        }
        return this.textureView;
    }
}

