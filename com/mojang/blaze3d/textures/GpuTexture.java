/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.textures;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;

@DontObfuscate
public abstract class GpuTexture
implements AutoCloseable {
    public static final int USAGE_COPY_DST = 1;
    public static final int USAGE_COPY_SRC = 2;
    public static final int USAGE_TEXTURE_BINDING = 4;
    public static final int USAGE_RENDER_ATTACHMENT = 8;
    public static final int USAGE_CUBEMAP_COMPATIBLE = 16;
    private final TextureFormat format;
    private final int width;
    private final int height;
    private final int depthOrLayers;
    private final int mipLevels;
    private final int usage;
    private final String label;
    protected AddressMode addressModeU = AddressMode.REPEAT;
    protected AddressMode addressModeV = AddressMode.REPEAT;
    protected FilterMode minFilter = FilterMode.NEAREST;
    protected FilterMode magFilter = FilterMode.LINEAR;
    protected boolean useMipmaps = true;

    public GpuTexture(int n, String string, TextureFormat textureFormat, int n2, int n3, int n4, int n5) {
        this.usage = n;
        this.label = string;
        this.format = textureFormat;
        this.width = n2;
        this.height = n3;
        this.depthOrLayers = n4;
        this.mipLevels = n5;
    }

    public int getWidth(int n) {
        return this.width >> n;
    }

    public int getHeight(int n) {
        return this.height >> n;
    }

    public int getDepthOrLayers() {
        return this.depthOrLayers;
    }

    public int getMipLevels() {
        return this.mipLevels;
    }

    public TextureFormat getFormat() {
        return this.format;
    }

    public int usage() {
        return this.usage;
    }

    public void setAddressMode(AddressMode addressMode) {
        this.setAddressMode(addressMode, addressMode);
    }

    public void setAddressMode(AddressMode addressMode, AddressMode addressMode2) {
        this.addressModeU = addressMode;
        this.addressModeV = addressMode2;
    }

    public void setTextureFilter(FilterMode filterMode, boolean bl) {
        this.setTextureFilter(filterMode, filterMode, bl);
    }

    public void setTextureFilter(FilterMode filterMode, FilterMode filterMode2, boolean bl) {
        this.minFilter = filterMode;
        this.magFilter = filterMode2;
        this.setUseMipmaps(bl);
    }

    public void setUseMipmaps(boolean bl) {
        this.useMipmaps = bl;
    }

    public String getLabel() {
        return this.label;
    }

    @Override
    public abstract void close();

    public abstract boolean isClosed();
}

