/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.client.resources.metadata.gui;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.server.packs.metadata.MetadataSectionType;

public record GuiMetadataSection(GuiSpriteScaling scaling) {
    public static final GuiMetadataSection DEFAULT = new GuiMetadataSection(GuiSpriteScaling.DEFAULT);
    public static final Codec<GuiMetadataSection> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)GuiSpriteScaling.CODEC.optionalFieldOf("scaling", (Object)GuiSpriteScaling.DEFAULT).forGetter(GuiMetadataSection::scaling)).apply((Applicative)instance, GuiMetadataSection::new));
    public static final MetadataSectionType<GuiMetadataSection> TYPE = new MetadataSectionType<GuiMetadataSection>("gui", CODEC);
}

