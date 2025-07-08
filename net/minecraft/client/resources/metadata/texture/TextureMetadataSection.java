/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.client.resources.metadata.texture;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.packs.metadata.MetadataSectionType;

public record TextureMetadataSection(boolean blur, boolean clamp) {
    public static final boolean DEFAULT_BLUR = false;
    public static final boolean DEFAULT_CLAMP = false;
    public static final Codec<TextureMetadataSection> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("blur", (Object)false).forGetter(TextureMetadataSection::blur), (App)Codec.BOOL.optionalFieldOf("clamp", (Object)false).forGetter(TextureMetadataSection::clamp)).apply((Applicative)instance, TextureMetadataSection::new));
    public static final MetadataSectionType<TextureMetadataSection> TYPE = new MetadataSectionType<TextureMetadataSection>("texture", CODEC);
}

