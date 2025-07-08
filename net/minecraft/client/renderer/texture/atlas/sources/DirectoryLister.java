/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public record DirectoryLister(String sourcePath, String idPrefix) implements SpriteSource
{
    public static final MapCodec<DirectoryLister> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("source").forGetter(DirectoryLister::sourcePath), (App)Codec.STRING.fieldOf("prefix").forGetter(DirectoryLister::idPrefix)).apply((Applicative)instance, DirectoryLister::new));

    @Override
    public void run(ResourceManager resourceManager, SpriteSource.Output output) {
        FileToIdConverter fileToIdConverter = new FileToIdConverter("textures/" + this.sourcePath, ".png");
        fileToIdConverter.listMatchingResources(resourceManager).forEach((resourceLocation, resource) -> {
            ResourceLocation resourceLocation2 = fileToIdConverter.fileToId((ResourceLocation)resourceLocation).withPrefix(this.idPrefix);
            output.add(resourceLocation2, (Resource)resource);
        });
    }

    public MapCodec<DirectoryLister> codec() {
        return MAP_CODEC;
    }
}

