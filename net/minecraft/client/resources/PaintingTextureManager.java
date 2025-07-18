/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.client.resources.model.AtlasIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.PaintingVariant;

public class PaintingTextureManager
extends TextureAtlasHolder {
    private static final ResourceLocation BACK_SPRITE_LOCATION = ResourceLocation.withDefaultNamespace("back");

    public PaintingTextureManager(TextureManager textureManager) {
        super(textureManager, ResourceLocation.withDefaultNamespace("textures/atlas/paintings.png"), AtlasIds.PAINTINGS);
    }

    public TextureAtlasSprite get(PaintingVariant paintingVariant) {
        return this.getSprite(paintingVariant.assetId());
    }

    public TextureAtlasSprite getBackSprite() {
        return this.getSprite(BACK_SPRITE_LOCATION);
    }
}

