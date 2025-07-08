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
package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;

public record RandomGroupPoolAlias(WeightedList<List<PoolAliasBinding>> groups) implements PoolAliasBinding
{
    static MapCodec<RandomGroupPoolAlias> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)WeightedList.nonEmptyCodec(Codec.list(PoolAliasBinding.CODEC)).fieldOf("groups").forGetter(RandomGroupPoolAlias::groups)).apply((Applicative)instance, RandomGroupPoolAlias::new));

    @Override
    public void forEachResolved(RandomSource randomSource, BiConsumer<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> biConsumer) {
        this.groups.getRandom(randomSource).ifPresent(list -> list.forEach(poolAliasBinding -> poolAliasBinding.forEachResolved(randomSource, biConsumer)));
    }

    @Override
    public Stream<ResourceKey<StructureTemplatePool>> allTargets() {
        return this.groups.unwrap().stream().flatMap(weighted -> ((List)weighted.value()).stream()).flatMap(PoolAliasBinding::allTargets);
    }

    public MapCodec<RandomGroupPoolAlias> codec() {
        return CODEC;
    }
}

