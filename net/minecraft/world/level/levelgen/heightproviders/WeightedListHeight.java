/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;

public class WeightedListHeight
extends HeightProvider {
    public static final MapCodec<WeightedListHeight> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)WeightedList.nonEmptyCodec(HeightProvider.CODEC).fieldOf("distribution").forGetter(weightedListHeight -> weightedListHeight.distribution)).apply((Applicative)instance, WeightedListHeight::new));
    private final WeightedList<HeightProvider> distribution;

    public WeightedListHeight(WeightedList<HeightProvider> weightedList) {
        this.distribution = weightedList;
    }

    @Override
    public int sample(RandomSource randomSource, WorldGenerationContext worldGenerationContext) {
        return this.distribution.getRandomOrThrow(randomSource).sample(randomSource, worldGenerationContext);
    }

    @Override
    public HeightProviderType<?> getType() {
        return HeightProviderType.WEIGHTED_LIST;
    }
}

