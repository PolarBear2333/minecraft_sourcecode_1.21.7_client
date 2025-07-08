/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen.features;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ClampedNormalFloat;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.MultifaceSpreadeableBlock;
import net.minecraft.world.level.block.SmallDripleafBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FossilFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MultifaceGrowthConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.PointedDripstoneConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SculkPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.UnderwaterMagmaConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class CaveFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> MONSTER_ROOM = FeatureUtils.createKey("monster_room");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FOSSIL_COAL = FeatureUtils.createKey("fossil_coal");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FOSSIL_DIAMONDS = FeatureUtils.createKey("fossil_diamonds");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DRIPSTONE_CLUSTER = FeatureUtils.createKey("dripstone_cluster");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LARGE_DRIPSTONE = FeatureUtils.createKey("large_dripstone");
    public static final ResourceKey<ConfiguredFeature<?, ?>> POINTED_DRIPSTONE = FeatureUtils.createKey("pointed_dripstone");
    public static final ResourceKey<ConfiguredFeature<?, ?>> UNDERWATER_MAGMA = FeatureUtils.createKey("underwater_magma");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GLOW_LICHEN = FeatureUtils.createKey("glow_lichen");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ROOTED_AZALEA_TREE = FeatureUtils.createKey("rooted_azalea_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CAVE_VINE = FeatureUtils.createKey("cave_vine");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CAVE_VINE_IN_MOSS = FeatureUtils.createKey("cave_vine_in_moss");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOSS_VEGETATION = FeatureUtils.createKey("moss_vegetation");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOSS_PATCH = FeatureUtils.createKey("moss_patch");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOSS_PATCH_BONEMEAL = FeatureUtils.createKey("moss_patch_bonemeal");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DRIPLEAF = FeatureUtils.createKey("dripleaf");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CLAY_WITH_DRIPLEAVES = FeatureUtils.createKey("clay_with_dripleaves");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CLAY_POOL_WITH_DRIPLEAVES = FeatureUtils.createKey("clay_pool_with_dripleaves");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LUSH_CAVES_CLAY = FeatureUtils.createKey("lush_caves_clay");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOSS_PATCH_CEILING = FeatureUtils.createKey("moss_patch_ceiling");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SPORE_BLOSSOM = FeatureUtils.createKey("spore_blossom");
    public static final ResourceKey<ConfiguredFeature<?, ?>> AMETHYST_GEODE = FeatureUtils.createKey("amethyst_geode");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SCULK_PATCH_DEEP_DARK = FeatureUtils.createKey("sculk_patch_deep_dark");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SCULK_PATCH_ANCIENT_CITY = FeatureUtils.createKey("sculk_patch_ancient_city");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SCULK_VEIN = FeatureUtils.createKey("sculk_vein");

    private static Holder<PlacedFeature> makeDripleaf(Direction direction) {
        return PlacementUtils.inlinePlaced(Feature.BLOCK_COLUMN, new BlockColumnConfiguration(List.of(BlockColumnConfiguration.layer(new WeightedListInt(WeightedList.builder().add(UniformInt.of(0, 4), 2).add((UniformInt)((Object)ConstantInt.of(0)), 1).build()), BlockStateProvider.simple((BlockState)Blocks.BIG_DRIPLEAF_STEM.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, direction))), BlockColumnConfiguration.layer(ConstantInt.of(1), BlockStateProvider.simple((BlockState)Blocks.BIG_DRIPLEAF.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, direction)))), Direction.UP, BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, true), new PlacementModifier[0]);
    }

    private static Holder<PlacedFeature> makeSmallDripleaf() {
        return PlacementUtils.inlinePlaced(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new WeightedStateProvider(WeightedList.builder().add((BlockState)Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, Direction.EAST), 1).add((BlockState)Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, Direction.WEST), 1).add((BlockState)Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, Direction.NORTH), 1).add((BlockState)Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, Direction.SOUTH), 1))), new PlacementModifier[0]);
    }

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> bootstrapContext) {
        HolderGetter<ConfiguredFeature<?, ?>> holderGetter = bootstrapContext.lookup(Registries.CONFIGURED_FEATURE);
        HolderGetter<StructureProcessorList> holderGetter2 = bootstrapContext.lookup(Registries.PROCESSOR_LIST);
        FeatureUtils.register(bootstrapContext, MONSTER_ROOM, Feature.MONSTER_ROOM);
        List<ResourceLocation> list = List.of(ResourceLocation.withDefaultNamespace("fossil/spine_1"), ResourceLocation.withDefaultNamespace("fossil/spine_2"), ResourceLocation.withDefaultNamespace("fossil/spine_3"), ResourceLocation.withDefaultNamespace("fossil/spine_4"), ResourceLocation.withDefaultNamespace("fossil/skull_1"), ResourceLocation.withDefaultNamespace("fossil/skull_2"), ResourceLocation.withDefaultNamespace("fossil/skull_3"), ResourceLocation.withDefaultNamespace("fossil/skull_4"));
        List<ResourceLocation> list2 = List.of(ResourceLocation.withDefaultNamespace("fossil/spine_1_coal"), ResourceLocation.withDefaultNamespace("fossil/spine_2_coal"), ResourceLocation.withDefaultNamespace("fossil/spine_3_coal"), ResourceLocation.withDefaultNamespace("fossil/spine_4_coal"), ResourceLocation.withDefaultNamespace("fossil/skull_1_coal"), ResourceLocation.withDefaultNamespace("fossil/skull_2_coal"), ResourceLocation.withDefaultNamespace("fossil/skull_3_coal"), ResourceLocation.withDefaultNamespace("fossil/skull_4_coal"));
        Holder.Reference<StructureProcessorList> reference = holderGetter2.getOrThrow(ProcessorLists.FOSSIL_ROT);
        FeatureUtils.register(bootstrapContext, FOSSIL_COAL, Feature.FOSSIL, new FossilFeatureConfiguration(list, list2, reference, holderGetter2.getOrThrow(ProcessorLists.FOSSIL_COAL), 4));
        FeatureUtils.register(bootstrapContext, FOSSIL_DIAMONDS, Feature.FOSSIL, new FossilFeatureConfiguration(list, list2, reference, holderGetter2.getOrThrow(ProcessorLists.FOSSIL_DIAMONDS), 4));
        FeatureUtils.register(bootstrapContext, DRIPSTONE_CLUSTER, Feature.DRIPSTONE_CLUSTER, new DripstoneClusterConfiguration(12, UniformInt.of(3, 6), UniformInt.of(2, 8), 1, 3, UniformInt.of(2, 4), UniformFloat.of(0.3f, 0.7f), ClampedNormalFloat.of(0.1f, 0.3f, 0.1f, 0.9f), 0.1f, 3, 8));
        FeatureUtils.register(bootstrapContext, LARGE_DRIPSTONE, Feature.LARGE_DRIPSTONE, new LargeDripstoneConfiguration(30, UniformInt.of(3, 19), UniformFloat.of(0.4f, 2.0f), 0.33f, UniformFloat.of(0.3f, 0.9f), UniformFloat.of(0.4f, 1.0f), UniformFloat.of(0.0f, 0.3f), 4, 0.6f));
        FeatureUtils.register(bootstrapContext, POINTED_DRIPSTONE, Feature.SIMPLE_RANDOM_SELECTOR, new SimpleRandomFeatureConfiguration(HolderSet.direct(PlacementUtils.inlinePlaced(Feature.POINTED_DRIPSTONE, new PointedDripstoneConfiguration(0.2f, 0.7f, 0.5f, 0.5f), EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, 12), RandomOffsetPlacement.vertical(ConstantInt.of(1))), PlacementUtils.inlinePlaced(Feature.POINTED_DRIPSTONE, new PointedDripstoneConfiguration(0.2f, 0.7f, 0.5f, 0.5f), EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, 12), RandomOffsetPlacement.vertical(ConstantInt.of(-1))))));
        FeatureUtils.register(bootstrapContext, UNDERWATER_MAGMA, Feature.UNDERWATER_MAGMA, new UnderwaterMagmaConfiguration(5, 1, 0.5f));
        MultifaceSpreadeableBlock multifaceSpreadeableBlock = (MultifaceSpreadeableBlock)Blocks.GLOW_LICHEN;
        FeatureUtils.register(bootstrapContext, GLOW_LICHEN, Feature.MULTIFACE_GROWTH, new MultifaceGrowthConfiguration(multifaceSpreadeableBlock, 20, false, true, true, 0.5f, HolderSet.direct(Block::builtInRegistryHolder, Blocks.STONE, Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE, Blocks.DRIPSTONE_BLOCK, Blocks.CALCITE, Blocks.TUFF, Blocks.DEEPSLATE)));
        FeatureUtils.register(bootstrapContext, ROOTED_AZALEA_TREE, Feature.ROOT_SYSTEM, new RootSystemConfiguration(PlacementUtils.inlinePlaced(holderGetter.getOrThrow(TreeFeatures.AZALEA_TREE), new PlacementModifier[0]), 3, 3, BlockTags.AZALEA_ROOT_REPLACEABLE, BlockStateProvider.simple(Blocks.ROOTED_DIRT), 20, 100, 3, 2, BlockStateProvider.simple(Blocks.HANGING_ROOTS), 20, 2, BlockPredicate.allOf(BlockPredicate.anyOf(BlockPredicate.matchesBlocks(List.of(Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR)), BlockPredicate.matchesTag(BlockTags.REPLACEABLE_BY_TREES)), BlockPredicate.matchesTag(Direction.DOWN.getUnitVec3i(), BlockTags.AZALEA_GROWS_ON))));
        WeightedStateProvider weightedStateProvider = new WeightedStateProvider(WeightedList.builder().add(Blocks.CAVE_VINES_PLANT.defaultBlockState(), 4).add((BlockState)Blocks.CAVE_VINES_PLANT.defaultBlockState().setValue(CaveVines.BERRIES, true), 1));
        RandomizedIntStateProvider randomizedIntStateProvider = new RandomizedIntStateProvider((BlockStateProvider)new WeightedStateProvider(WeightedList.builder().add(Blocks.CAVE_VINES.defaultBlockState(), 4).add((BlockState)Blocks.CAVE_VINES.defaultBlockState().setValue(CaveVines.BERRIES, true), 1)), CaveVinesBlock.AGE, (IntProvider)UniformInt.of(23, 25));
        FeatureUtils.register(bootstrapContext, CAVE_VINE, Feature.BLOCK_COLUMN, new BlockColumnConfiguration(List.of(BlockColumnConfiguration.layer(new WeightedListInt(WeightedList.builder().add(UniformInt.of(0, 19), 2).add(UniformInt.of(0, 2), 3).add(UniformInt.of(0, 6), 10).build()), weightedStateProvider), BlockColumnConfiguration.layer(ConstantInt.of(1), randomizedIntStateProvider)), Direction.DOWN, BlockPredicate.ONLY_IN_AIR_PREDICATE, true));
        FeatureUtils.register(bootstrapContext, CAVE_VINE_IN_MOSS, Feature.BLOCK_COLUMN, new BlockColumnConfiguration(List.of(BlockColumnConfiguration.layer(new WeightedListInt(WeightedList.builder().add(UniformInt.of(0, 3), 5).add(UniformInt.of(1, 7), 1).build()), weightedStateProvider), BlockColumnConfiguration.layer(ConstantInt.of(1), randomizedIntStateProvider)), Direction.DOWN, BlockPredicate.ONLY_IN_AIR_PREDICATE, true));
        FeatureUtils.register(bootstrapContext, MOSS_VEGETATION, Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new WeightedStateProvider(WeightedList.builder().add(Blocks.FLOWERING_AZALEA.defaultBlockState(), 4).add(Blocks.AZALEA.defaultBlockState(), 7).add(Blocks.MOSS_CARPET.defaultBlockState(), 25).add(Blocks.SHORT_GRASS.defaultBlockState(), 50).add(Blocks.TALL_GRASS.defaultBlockState(), 10))));
        FeatureUtils.register(bootstrapContext, MOSS_PATCH, Feature.VEGETATION_PATCH, new VegetationPatchConfiguration(BlockTags.MOSS_REPLACEABLE, BlockStateProvider.simple(Blocks.MOSS_BLOCK), PlacementUtils.inlinePlaced(holderGetter.getOrThrow(MOSS_VEGETATION), new PlacementModifier[0]), CaveSurface.FLOOR, ConstantInt.of(1), 0.0f, 5, 0.8f, UniformInt.of(4, 7), 0.3f));
        FeatureUtils.register(bootstrapContext, MOSS_PATCH_BONEMEAL, Feature.VEGETATION_PATCH, new VegetationPatchConfiguration(BlockTags.MOSS_REPLACEABLE, BlockStateProvider.simple(Blocks.MOSS_BLOCK), PlacementUtils.inlinePlaced(holderGetter.getOrThrow(MOSS_VEGETATION), new PlacementModifier[0]), CaveSurface.FLOOR, ConstantInt.of(1), 0.0f, 5, 0.6f, UniformInt.of(1, 2), 0.75f));
        FeatureUtils.register(bootstrapContext, DRIPLEAF, Feature.SIMPLE_RANDOM_SELECTOR, new SimpleRandomFeatureConfiguration(HolderSet.direct(CaveFeatures.makeSmallDripleaf(), CaveFeatures.makeDripleaf(Direction.EAST), CaveFeatures.makeDripleaf(Direction.WEST), CaveFeatures.makeDripleaf(Direction.SOUTH), CaveFeatures.makeDripleaf(Direction.NORTH))));
        FeatureUtils.register(bootstrapContext, CLAY_WITH_DRIPLEAVES, Feature.VEGETATION_PATCH, new VegetationPatchConfiguration(BlockTags.LUSH_GROUND_REPLACEABLE, BlockStateProvider.simple(Blocks.CLAY), PlacementUtils.inlinePlaced(holderGetter.getOrThrow(DRIPLEAF), new PlacementModifier[0]), CaveSurface.FLOOR, ConstantInt.of(3), 0.8f, 2, 0.05f, UniformInt.of(4, 7), 0.7f));
        FeatureUtils.register(bootstrapContext, CLAY_POOL_WITH_DRIPLEAVES, Feature.WATERLOGGED_VEGETATION_PATCH, new VegetationPatchConfiguration(BlockTags.LUSH_GROUND_REPLACEABLE, BlockStateProvider.simple(Blocks.CLAY), PlacementUtils.inlinePlaced(holderGetter.getOrThrow(DRIPLEAF), new PlacementModifier[0]), CaveSurface.FLOOR, ConstantInt.of(3), 0.8f, 5, 0.1f, UniformInt.of(4, 7), 0.7f));
        FeatureUtils.register(bootstrapContext, LUSH_CAVES_CLAY, Feature.RANDOM_BOOLEAN_SELECTOR, new RandomBooleanFeatureConfiguration(PlacementUtils.inlinePlaced(holderGetter.getOrThrow(CLAY_WITH_DRIPLEAVES), new PlacementModifier[0]), PlacementUtils.inlinePlaced(holderGetter.getOrThrow(CLAY_POOL_WITH_DRIPLEAVES), new PlacementModifier[0])));
        FeatureUtils.register(bootstrapContext, MOSS_PATCH_CEILING, Feature.VEGETATION_PATCH, new VegetationPatchConfiguration(BlockTags.MOSS_REPLACEABLE, BlockStateProvider.simple(Blocks.MOSS_BLOCK), PlacementUtils.inlinePlaced(holderGetter.getOrThrow(CAVE_VINE_IN_MOSS), new PlacementModifier[0]), CaveSurface.CEILING, UniformInt.of(1, 2), 0.0f, 5, 0.08f, UniformInt.of(4, 7), 0.3f));
        FeatureUtils.register(bootstrapContext, SPORE_BLOSSOM, Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.SPORE_BLOSSOM)));
        FeatureUtils.register(bootstrapContext, AMETHYST_GEODE, Feature.GEODE, new GeodeConfiguration(new GeodeBlockSettings(BlockStateProvider.simple(Blocks.AIR), BlockStateProvider.simple(Blocks.AMETHYST_BLOCK), BlockStateProvider.simple(Blocks.BUDDING_AMETHYST), BlockStateProvider.simple(Blocks.CALCITE), BlockStateProvider.simple(Blocks.SMOOTH_BASALT), List.of(Blocks.SMALL_AMETHYST_BUD.defaultBlockState(), Blocks.MEDIUM_AMETHYST_BUD.defaultBlockState(), Blocks.LARGE_AMETHYST_BUD.defaultBlockState(), Blocks.AMETHYST_CLUSTER.defaultBlockState()), BlockTags.FEATURES_CANNOT_REPLACE, BlockTags.GEODE_INVALID_BLOCKS), new GeodeLayerSettings(1.7, 2.2, 3.2, 4.2), new GeodeCrackSettings(0.95, 2.0, 2), 0.35, 0.083, true, UniformInt.of(4, 6), UniformInt.of(3, 4), UniformInt.of(1, 2), -16, 16, 0.05, 1));
        FeatureUtils.register(bootstrapContext, SCULK_PATCH_DEEP_DARK, Feature.SCULK_PATCH, new SculkPatchConfiguration(10, 32, 64, 0, 1, ConstantInt.of(0), 0.5f));
        FeatureUtils.register(bootstrapContext, SCULK_PATCH_ANCIENT_CITY, Feature.SCULK_PATCH, new SculkPatchConfiguration(10, 32, 64, 0, 1, UniformInt.of(1, 3), 0.5f));
        MultifaceSpreadeableBlock multifaceSpreadeableBlock2 = (MultifaceSpreadeableBlock)Blocks.SCULK_VEIN;
        FeatureUtils.register(bootstrapContext, SCULK_VEIN, Feature.MULTIFACE_GROWTH, new MultifaceGrowthConfiguration(multifaceSpreadeableBlock2, 20, true, true, true, 1.0f, HolderSet.direct(Block::builtInRegistryHolder, Blocks.STONE, Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE, Blocks.DRIPSTONE_BLOCK, Blocks.CALCITE, Blocks.TUFF, Blocks.DEEPSLATE)));
    }
}

