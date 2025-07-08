/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2IntMap
 *  it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
 *  it.unimi.dsi.fastutil.ints.IntIterator
 *  javax.annotation.Nullable
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import javax.annotation.Nullable;

public class GlTexture
extends GpuTexture {
    protected final int id;
    private final Int2IntMap fboCache = new Int2IntOpenHashMap();
    protected boolean closed;
    protected boolean modesDirty = true;
    private int views;

    protected GlTexture(int n, String string, TextureFormat textureFormat, int n2, int n3, int n4, int n5, int n6) {
        super(n, string, textureFormat, n2, n3, n4, n5);
        this.id = n6;
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        if (this.views == 0) {
            this.destroyImmediately();
        }
    }

    private void destroyImmediately() {
        GlStateManager._deleteTexture(this.id);
        IntIterator intIterator = this.fboCache.values().iterator();
        while (intIterator.hasNext()) {
            int n = (Integer)intIterator.next();
            GlStateManager._glDeleteFramebuffers(n);
        }
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    public int getFbo(DirectStateAccess directStateAccess, @Nullable GpuTexture gpuTexture) {
        int n = gpuTexture == null ? 0 : ((GlTexture)gpuTexture).id;
        return this.fboCache.computeIfAbsent(n, n2 -> {
            int n3 = directStateAccess.createFrameBufferObject();
            directStateAccess.bindFrameBufferTextures(n3, this.id, n, 0, 0);
            return n3;
        });
    }

    public void flushModeChanges(int n) {
        if (this.modesDirty) {
            GlStateManager._texParameter(n, 10242, GlConst.toGl(this.addressModeU));
            GlStateManager._texParameter(n, 10243, GlConst.toGl(this.addressModeV));
            switch (this.minFilter) {
                case NEAREST: {
                    GlStateManager._texParameter(n, 10241, this.useMipmaps ? 9986 : 9728);
                    break;
                }
                case LINEAR: {
                    GlStateManager._texParameter(n, 10241, this.useMipmaps ? 9987 : 9729);
                }
            }
            switch (this.magFilter) {
                case NEAREST: {
                    GlStateManager._texParameter(n, 10240, 9728);
                    break;
                }
                case LINEAR: {
                    GlStateManager._texParameter(n, 10240, 9729);
                }
            }
            this.modesDirty = false;
        }
    }

    public int glId() {
        return this.id;
    }

    @Override
    public void setAddressMode(AddressMode addressMode, AddressMode addressMode2) {
        super.setAddressMode(addressMode, addressMode2);
        this.modesDirty = true;
    }

    @Override
    public void setTextureFilter(FilterMode filterMode, FilterMode filterMode2, boolean bl) {
        super.setTextureFilter(filterMode, filterMode2, bl);
        this.modesDirty = true;
    }

    @Override
    public void setUseMipmaps(boolean bl) {
        super.setUseMipmaps(bl);
        this.modesDirty = true;
    }

    public void addViews() {
        ++this.views;
    }

    public void removeViews() {
        --this.views;
        if (this.closed && this.views == 0) {
            this.destroyImmediately();
        }
    }
}

