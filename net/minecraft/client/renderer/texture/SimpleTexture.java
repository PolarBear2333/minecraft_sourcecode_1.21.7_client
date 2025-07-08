/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.texture;

import java.io.IOException;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class SimpleTexture
extends ReloadableTexture {
    public SimpleTexture(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override
    public TextureContents loadContents(ResourceManager resourceManager) throws IOException {
        return TextureContents.load(resourceManager, this.resourceId());
    }
}

