/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

public record SingleFile(ResourceLocation resourceId, Optional<ResourceLocation> spriteId) implements SpriteSource
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<SingleFile> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("resource").forGetter(SingleFile::resourceId), (App)ResourceLocation.CODEC.optionalFieldOf("sprite").forGetter(SingleFile::spriteId)).apply((Applicative)instance, SingleFile::new));

    public SingleFile(ResourceLocation resourceLocation) {
        this(resourceLocation, Optional.empty());
    }

    @Override
    public void run(ResourceManager resourceManager, SpriteSource.Output output) {
        ResourceLocation resourceLocation = TEXTURE_ID_CONVERTER.idToFile(this.resourceId);
        Optional<Resource> optional = resourceManager.getResource(resourceLocation);
        if (optional.isPresent()) {
            output.add(this.spriteId.orElse(this.resourceId), optional.get());
        } else {
            LOGGER.warn("Missing sprite: {}", (Object)resourceLocation);
        }
    }

    public MapCodec<SingleFile> codec() {
        return MAP_CODEC;
    }
}

