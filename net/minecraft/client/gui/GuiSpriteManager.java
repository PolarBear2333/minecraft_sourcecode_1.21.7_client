/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui;

import java.util.Set;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.client.resources.model.AtlasIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionType;

public class GuiSpriteManager
extends TextureAtlasHolder {
    private static final Set<MetadataSectionType<?>> METADATA_SECTIONS = Set.of(AnimationMetadataSection.TYPE, GuiMetadataSection.TYPE);

    public GuiSpriteManager(TextureManager textureManager) {
        super(textureManager, ResourceLocation.withDefaultNamespace("textures/atlas/gui.png"), AtlasIds.GUI, METADATA_SECTIONS);
    }

    @Override
    public TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
        return super.getSprite(resourceLocation);
    }

    public GuiSpriteScaling getSpriteScaling(TextureAtlasSprite textureAtlasSprite) {
        return this.getMetadata(textureAtlasSprite).scaling();
    }

    private GuiMetadataSection getMetadata(TextureAtlasSprite textureAtlasSprite) {
        return textureAtlasSprite.contents().metadata().getSection(GuiMetadataSection.TYPE).orElse(GuiMetadataSection.DEFAULT);
    }
}

