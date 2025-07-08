/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class DualNoiseProvider
extends NoiseProvider {
    public static final MapCodec<DualNoiseProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)InclusiveRange.codec(Codec.INT, 1, 64).fieldOf("variety").forGetter(dualNoiseProvider -> dualNoiseProvider.variety), (App)NormalNoise.NoiseParameters.DIRECT_CODEC.fieldOf("slow_noise").forGetter(dualNoiseProvider -> dualNoiseProvider.slowNoiseParameters), (App)ExtraCodecs.POSITIVE_FLOAT.fieldOf("slow_scale").forGetter(dualNoiseProvider -> Float.valueOf(dualNoiseProvider.slowScale))).and(DualNoiseProvider.noiseProviderCodec(instance)).apply((Applicative)instance, DualNoiseProvider::new));
    private final InclusiveRange<Integer> variety;
    private final NormalNoise.NoiseParameters slowNoiseParameters;
    private final float slowScale;
    private final NormalNoise slowNoise;

    public DualNoiseProvider(InclusiveRange<Integer> inclusiveRange, NormalNoise.NoiseParameters noiseParameters, float f, long l, NormalNoise.NoiseParameters noiseParameters2, float f2, List<BlockState> list) {
        super(l, noiseParameters2, f2, list);
        this.variety = inclusiveRange;
        this.slowNoiseParameters = noiseParameters;
        this.slowScale = f;
        this.slowNoise = NormalNoise.create(new WorldgenRandom(new LegacyRandomSource(l)), noiseParameters);
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.DUAL_NOISE_PROVIDER;
    }

    @Override
    public BlockState getState(RandomSource randomSource, BlockPos blockPos) {
        double d = this.getSlowNoiseValue(blockPos);
        int n = (int)Mth.clampedMap(d, -1.0, 1.0, (double)this.variety.minInclusive().intValue(), (double)(this.variety.maxInclusive() + 1));
        ArrayList arrayList = Lists.newArrayListWithCapacity((int)n);
        for (int i = 0; i < n; ++i) {
            arrayList.add(this.getRandomState(this.states, this.getSlowNoiseValue(blockPos.offset(i * 54545, 0, i * 34234))));
        }
        return this.getRandomState(arrayList, blockPos, this.scale);
    }

    protected double getSlowNoiseValue(BlockPos blockPos) {
        return this.slowNoise.getValue((float)blockPos.getX() * this.slowScale, (float)blockPos.getY() * this.slowScale, (float)blockPos.getZ() * this.slowScale);
    }
}

