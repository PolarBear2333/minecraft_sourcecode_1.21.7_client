/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen.placement;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.NetherFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.BiasedToBottomInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountOnEveryLayerPlacement;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class NetherPlacements {
    public static final ResourceKey<PlacedFeature> DELTA = PlacementUtils.createKey("delta");
    public static final ResourceKey<PlacedFeature> SMALL_BASALT_COLUMNS = PlacementUtils.createKey("small_basalt_columns");
    public static final ResourceKey<PlacedFeature> LARGE_BASALT_COLUMNS = PlacementUtils.createKey("large_basalt_columns");
    public static final ResourceKey<PlacedFeature> BASALT_BLOBS = PlacementUtils.createKey("basalt_blobs");
    public static final ResourceKey<PlacedFeature> BLACKSTONE_BLOBS = PlacementUtils.createKey("blackstone_blobs");
    public static final ResourceKey<PlacedFeature> GLOWSTONE_EXTRA = PlacementUtils.createKey("glowstone_extra");
    public static final ResourceKey<PlacedFeature> GLOWSTONE = PlacementUtils.createKey("glowstone");
    public static final ResourceKey<PlacedFeature> CRIMSON_FOREST_VEGETATION = PlacementUtils.createKey("crimson_forest_vegetation");
    public static final ResourceKey<PlacedFeature> WARPED_FOREST_VEGETATION = PlacementUtils.createKey("warped_forest_vegetation");
    public static final ResourceKey<PlacedFeature> NETHER_SPROUTS = PlacementUtils.createKey("nether_sprouts");
    public static final ResourceKey<PlacedFeature> TWISTING_VINES = PlacementUtils.createKey("twisting_vines");
    public static final ResourceKey<PlacedFeature> WEEPING_VINES = PlacementUtils.createKey("weeping_vines");
    public static final ResourceKey<PlacedFeature> PATCH_CRIMSON_ROOTS = PlacementUtils.createKey("patch_crimson_roots");
    public static final ResourceKey<PlacedFeature> BASALT_PILLAR = PlacementUtils.createKey("basalt_pillar");
    public static final ResourceKey<PlacedFeature> SPRING_DELTA = PlacementUtils.createKey("spring_delta");
    public static final ResourceKey<PlacedFeature> SPRING_CLOSED = PlacementUtils.createKey("spring_closed");
    public static final ResourceKey<PlacedFeature> SPRING_CLOSED_DOUBLE = PlacementUtils.createKey("spring_closed_double");
    public static final ResourceKey<PlacedFeature> SPRING_OPEN = PlacementUtils.createKey("spring_open");
    public static final ResourceKey<PlacedFeature> PATCH_SOUL_FIRE = PlacementUtils.createKey("patch_soul_fire");
    public static final ResourceKey<PlacedFeature> PATCH_FIRE = PlacementUtils.createKey("patch_fire");

    public static void bootstrap(BootstrapContext<PlacedFeature> bootstrapContext) {
        HolderGetter<ConfiguredFeature<?, ?>> holderGetter = bootstrapContext.lookup(Registries.CONFIGURED_FEATURE);
        Holder.Reference<ConfiguredFeature<?, ?>> reference = holderGetter.getOrThrow(NetherFeatures.DELTA);
        Holder.Reference<ConfiguredFeature<?, ?>> reference2 = holderGetter.getOrThrow(NetherFeatures.SMALL_BASALT_COLUMNS);
        Holder.Reference<ConfiguredFeature<?, ?>> reference3 = holderGetter.getOrThrow(NetherFeatures.LARGE_BASALT_COLUMNS);
        Holder.Reference<ConfiguredFeature<?, ?>> reference4 = holderGetter.getOrThrow(NetherFeatures.BASALT_BLOBS);
        Holder.Reference<ConfiguredFeature<?, ?>> reference5 = holderGetter.getOrThrow(NetherFeatures.BLACKSTONE_BLOBS);
        Holder.Reference<ConfiguredFeature<?, ?>> reference6 = holderGetter.getOrThrow(NetherFeatures.GLOWSTONE_EXTRA);
        Holder.Reference<ConfiguredFeature<?, ?>> reference7 = holderGetter.getOrThrow(NetherFeatures.CRIMSON_FOREST_VEGETATION);
        Holder.Reference<ConfiguredFeature<?, ?>> reference8 = holderGetter.getOrThrow(NetherFeatures.WARPED_FOREST_VEGETION);
        Holder.Reference<ConfiguredFeature<?, ?>> reference9 = holderGetter.getOrThrow(NetherFeatures.NETHER_SPROUTS);
        Holder.Reference<ConfiguredFeature<?, ?>> reference10 = holderGetter.getOrThrow(NetherFeatures.TWISTING_VINES);
        Holder.Reference<ConfiguredFeature<?, ?>> reference11 = holderGetter.getOrThrow(NetherFeatures.WEEPING_VINES);
        Holder.Reference<ConfiguredFeature<?, ?>> reference12 = holderGetter.getOrThrow(NetherFeatures.PATCH_CRIMSON_ROOTS);
        Holder.Reference<ConfiguredFeature<?, ?>> reference13 = holderGetter.getOrThrow(NetherFeatures.BASALT_PILLAR);
        Holder.Reference<ConfiguredFeature<?, ?>> reference14 = holderGetter.getOrThrow(NetherFeatures.SPRING_LAVA_NETHER);
        Holder.Reference<ConfiguredFeature<?, ?>> reference15 = holderGetter.getOrThrow(NetherFeatures.SPRING_NETHER_CLOSED);
        Holder.Reference<ConfiguredFeature<?, ?>> reference16 = holderGetter.getOrThrow(NetherFeatures.SPRING_NETHER_OPEN);
        Holder.Reference<ConfiguredFeature<?, ?>> reference17 = holderGetter.getOrThrow(NetherFeatures.PATCH_SOUL_FIRE);
        Holder.Reference<ConfiguredFeature<?, ?>> reference18 = holderGetter.getOrThrow(NetherFeatures.PATCH_FIRE);
        PlacementUtils.register(bootstrapContext, DELTA, reference, CountOnEveryLayerPlacement.of(40), BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, SMALL_BASALT_COLUMNS, reference2, CountOnEveryLayerPlacement.of(4), BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, LARGE_BASALT_COLUMNS, reference3, CountOnEveryLayerPlacement.of(2), BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, BASALT_BLOBS, reference4, CountPlacement.of(75), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, BLACKSTONE_BLOBS, reference5, CountPlacement.of(25), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, GLOWSTONE_EXTRA, reference6, CountPlacement.of(BiasedToBottomInt.of(0, 9)), InSquarePlacement.spread(), PlacementUtils.RANGE_4_4, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, GLOWSTONE, reference6, CountPlacement.of(10), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, CRIMSON_FOREST_VEGETATION, reference7, CountOnEveryLayerPlacement.of(6), BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, WARPED_FOREST_VEGETATION, reference8, CountOnEveryLayerPlacement.of(5), BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, NETHER_SPROUTS, reference9, CountOnEveryLayerPlacement.of(4), BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, TWISTING_VINES, reference10, CountPlacement.of(10), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, WEEPING_VINES, reference11, CountPlacement.of(10), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_CRIMSON_ROOTS, reference12, PlacementUtils.FULL_RANGE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, BASALT_PILLAR, reference13, CountPlacement.of(10), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, SPRING_DELTA, reference14, CountPlacement.of(16), InSquarePlacement.spread(), PlacementUtils.RANGE_4_4, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, SPRING_CLOSED, reference15, CountPlacement.of(16), InSquarePlacement.spread(), PlacementUtils.RANGE_10_10, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, SPRING_CLOSED_DOUBLE, reference15, CountPlacement.of(32), InSquarePlacement.spread(), PlacementUtils.RANGE_10_10, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, SPRING_OPEN, reference16, CountPlacement.of(8), InSquarePlacement.spread(), PlacementUtils.RANGE_4_4, BiomeFilter.biome());
        List<PlacementModifier> list = List.of(CountPlacement.of(UniformInt.of(0, 5)), InSquarePlacement.spread(), PlacementUtils.RANGE_4_4, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_SOUL_FIRE, reference17, list);
        PlacementUtils.register(bootstrapContext, PATCH_FIRE, reference18, list);
    }
}

