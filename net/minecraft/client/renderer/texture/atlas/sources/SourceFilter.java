/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ResourceLocationPattern;

public record SourceFilter(ResourceLocationPattern filter) implements SpriteSource
{
    public static final MapCodec<SourceFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceLocationPattern.CODEC.fieldOf("pattern").forGetter(SourceFilter::filter)).apply((Applicative)instance, SourceFilter::new));

    @Override
    public void run(ResourceManager resourceManager, SpriteSource.Output output) {
        output.removeAll(this.filter.locationPredicate());
    }

    public MapCodec<SourceFilter> codec() {
        return MAP_CODEC;
    }
}

