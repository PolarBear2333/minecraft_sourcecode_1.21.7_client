/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

public class WeightedListInt
extends IntProvider {
    public static final MapCodec<WeightedListInt> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)WeightedList.nonEmptyCodec(IntProvider.CODEC).fieldOf("distribution").forGetter(weightedListInt -> weightedListInt.distribution)).apply((Applicative)instance, WeightedListInt::new));
    private final WeightedList<IntProvider> distribution;
    private final int minValue;
    private final int maxValue;

    public WeightedListInt(WeightedList<IntProvider> weightedList) {
        this.distribution = weightedList;
        int n = Integer.MAX_VALUE;
        int n2 = Integer.MIN_VALUE;
        for (Weighted<IntProvider> weighted : weightedList.unwrap()) {
            int n3 = weighted.value().getMinValue();
            int n4 = weighted.value().getMaxValue();
            n = Math.min(n, n3);
            n2 = Math.max(n2, n4);
        }
        this.minValue = n;
        this.maxValue = n2;
    }

    @Override
    public int sample(RandomSource randomSource) {
        return this.distribution.getRandomOrThrow(randomSource).sample(randomSource);
    }

    @Override
    public int getMinValue() {
        return this.minValue;
    }

    @Override
    public int getMaxValue() {
        return this.maxValue;
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.WEIGHTED_LIST;
    }
}

