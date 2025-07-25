/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class HugeMushroomFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<HugeMushroomFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockStateProvider.CODEC.fieldOf("cap_provider").forGetter(hugeMushroomFeatureConfiguration -> hugeMushroomFeatureConfiguration.capProvider), (App)BlockStateProvider.CODEC.fieldOf("stem_provider").forGetter(hugeMushroomFeatureConfiguration -> hugeMushroomFeatureConfiguration.stemProvider), (App)Codec.INT.fieldOf("foliage_radius").orElse((Object)2).forGetter(hugeMushroomFeatureConfiguration -> hugeMushroomFeatureConfiguration.foliageRadius)).apply((Applicative)instance, HugeMushroomFeatureConfiguration::new));
    public final BlockStateProvider capProvider;
    public final BlockStateProvider stemProvider;
    public final int foliageRadius;

    public HugeMushroomFeatureConfiguration(BlockStateProvider blockStateProvider, BlockStateProvider blockStateProvider2, int n) {
        this.capProvider = blockStateProvider;
        this.stemProvider = blockStateProvider2;
        this.foliageRadius = n;
    }
}

