/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.textures.GpuTexture;

public interface SpriteTicker
extends AutoCloseable {
    public void tickAndUpload(int var1, int var2, GpuTexture var3);

    @Override
    public void close();
}

