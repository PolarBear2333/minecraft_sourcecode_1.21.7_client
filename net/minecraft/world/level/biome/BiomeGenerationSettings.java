/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.slf4j.Logger;

public class BiomeGenerationSettings {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final BiomeGenerationSettings EMPTY = new BiomeGenerationSettings(HolderSet.direct(new Holder[0]), List.of());
    public static final MapCodec<BiomeGenerationSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ConfiguredWorldCarver.LIST_CODEC.promotePartial(Util.prefix("Carver: ", arg_0 -> ((Logger)LOGGER).error(arg_0))).fieldOf("carvers").forGetter(biomeGenerationSettings -> biomeGenerationSettings.carvers), (App)PlacedFeature.LIST_OF_LISTS_CODEC.promotePartial(Util.prefix("Features: ", arg_0 -> ((Logger)LOGGER).error(arg_0))).fieldOf("features").forGetter(biomeGenerationSettings -> biomeGenerationSettings.features)).apply((Applicative)instance, BiomeGenerationSettings::new));
    private final HolderSet<ConfiguredWorldCarver<?>> carvers;
    private final List<HolderSet<PlacedFeature>> features;
    private final Supplier<List<ConfiguredFeature<?, ?>>> flowerFeatures;
    private final Supplier<Set<PlacedFeature>> featureSet;

    BiomeGenerationSettings(HolderSet<ConfiguredWorldCarver<?>> holderSet, List<HolderSet<PlacedFeature>> list) {
        this.carvers = holderSet;
        this.features = list;
        this.flowerFeatures = Suppliers.memoize(() -> (List)list.stream().flatMap(HolderSet::stream).map(Holder::value).flatMap(PlacedFeature::getFeatures).filter(configuredFeature -> configuredFeature.feature() == Feature.FLOWER).collect(ImmutableList.toImmutableList()));
        this.featureSet = Suppliers.memoize(() -> list.stream().flatMap(HolderSet::stream).map(Holder::value).collect(Collectors.toSet()));
    }

    public Iterable<Holder<ConfiguredWorldCarver<?>>> getCarvers() {
        return this.carvers;
    }

    public List<ConfiguredFeature<?, ?>> getFlowerFeatures() {
        return this.flowerFeatures.get();
    }

    public List<HolderSet<PlacedFeature>> features() {
        return this.features;
    }

    public boolean hasFeature(PlacedFeature placedFeature) {
        return this.featureSet.get().contains(placedFeature);
    }

    public static class Builder
    extends PlainBuilder {
        private final HolderGetter<PlacedFeature> placedFeatures;
        private final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers;

        public Builder(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
            this.placedFeatures = holderGetter;
            this.worldCarvers = holderGetter2;
        }

        public Builder addFeature(GenerationStep.Decoration decoration, ResourceKey<PlacedFeature> resourceKey) {
            this.addFeature(decoration.ordinal(), this.placedFeatures.getOrThrow(resourceKey));
            return this;
        }

        public Builder addCarver(ResourceKey<ConfiguredWorldCarver<?>> resourceKey) {
            this.addCarver(this.worldCarvers.getOrThrow(resourceKey));
            return this;
        }
    }

    public static class PlainBuilder {
        private final List<Holder<ConfiguredWorldCarver<?>>> carvers = new ArrayList();
        private final List<List<Holder<PlacedFeature>>> features = new ArrayList<List<Holder<PlacedFeature>>>();

        public PlainBuilder addFeature(GenerationStep.Decoration decoration, Holder<PlacedFeature> holder) {
            return this.addFeature(decoration.ordinal(), holder);
        }

        public PlainBuilder addFeature(int n, Holder<PlacedFeature> holder) {
            this.addFeatureStepsUpTo(n);
            this.features.get(n).add(holder);
            return this;
        }

        public PlainBuilder addCarver(Holder<ConfiguredWorldCarver<?>> holder) {
            this.carvers.add(holder);
            return this;
        }

        private void addFeatureStepsUpTo(int n) {
            while (this.features.size() <= n) {
                this.features.add(Lists.newArrayList());
            }
        }

        public BiomeGenerationSettings build() {
            return new BiomeGenerationSettings(HolderSet.direct(this.carvers), (List)this.features.stream().map(HolderSet::direct).collect(ImmutableList.toImmutableList()));
        }
    }
}

