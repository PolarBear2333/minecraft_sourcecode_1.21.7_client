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
package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.OptionalInt;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSizeType;

public class TwoLayersFeatureSize
extends FeatureSize {
    public static final MapCodec<TwoLayersFeatureSize> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.intRange((int)0, (int)81).fieldOf("limit").orElse((Object)1).forGetter(twoLayersFeatureSize -> twoLayersFeatureSize.limit), (App)Codec.intRange((int)0, (int)16).fieldOf("lower_size").orElse((Object)0).forGetter(twoLayersFeatureSize -> twoLayersFeatureSize.lowerSize), (App)Codec.intRange((int)0, (int)16).fieldOf("upper_size").orElse((Object)1).forGetter(twoLayersFeatureSize -> twoLayersFeatureSize.upperSize), TwoLayersFeatureSize.minClippedHeightCodec()).apply((Applicative)instance, TwoLayersFeatureSize::new));
    private final int limit;
    private final int lowerSize;
    private final int upperSize;

    public TwoLayersFeatureSize(int n, int n2, int n3) {
        this(n, n2, n3, OptionalInt.empty());
    }

    public TwoLayersFeatureSize(int n, int n2, int n3, OptionalInt optionalInt) {
        super(optionalInt);
        this.limit = n;
        this.lowerSize = n2;
        this.upperSize = n3;
    }

    @Override
    protected FeatureSizeType<?> type() {
        return FeatureSizeType.TWO_LAYERS_FEATURE_SIZE;
    }

    @Override
    public int getSizeAtHeight(int n, int n2) {
        return n2 < this.limit ? this.lowerSize : this.upperSize;
    }
}

