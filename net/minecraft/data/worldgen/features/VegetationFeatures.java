/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen.features;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.TreePlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.BiasedToBottomInt;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBedBlock;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.DualNoiseProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseThresholdProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.Fluids;

public class VegetationFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> BAMBOO_NO_PODZOL = FeatureUtils.createKey("bamboo_no_podzol");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BAMBOO_SOME_PODZOL = FeatureUtils.createKey("bamboo_some_podzol");
    public static final ResourceKey<ConfiguredFeature<?, ?>> VINES = FeatureUtils.createKey("vines");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_BROWN_MUSHROOM = FeatureUtils.createKey("patch_brown_mushroom");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_RED_MUSHROOM = FeatureUtils.createKey("patch_red_mushroom");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_SUNFLOWER = FeatureUtils.createKey("patch_sunflower");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_PUMPKIN = FeatureUtils.createKey("patch_pumpkin");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_BERRY_BUSH = FeatureUtils.createKey("patch_berry_bush");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_TAIGA_GRASS = FeatureUtils.createKey("patch_taiga_grass");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_GRASS = FeatureUtils.createKey("patch_grass");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_GRASS_MEADOW = FeatureUtils.createKey("patch_grass_meadow");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_GRASS_JUNGLE = FeatureUtils.createKey("patch_grass_jungle");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SINGLE_PIECE_OF_GRASS = FeatureUtils.createKey("single_piece_of_grass");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_DEAD_BUSH = FeatureUtils.createKey("patch_dead_bush");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_DRY_GRASS = FeatureUtils.createKey("patch_dry_grass");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_MELON = FeatureUtils.createKey("patch_melon");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_WATERLILY = FeatureUtils.createKey("patch_waterlily");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_TALL_GRASS = FeatureUtils.createKey("patch_tall_grass");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_LARGE_FERN = FeatureUtils.createKey("patch_large_fern");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_BUSH = FeatureUtils.createKey("patch_bush");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_LEAF_LITTER = FeatureUtils.createKey("patch_leaf_litter");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_FIREFLY_BUSH = FeatureUtils.createKey("patch_firefly_bush");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_CACTUS = FeatureUtils.createKey("patch_cactus");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_SUGAR_CANE = FeatureUtils.createKey("patch_sugar_cane");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOWER_DEFAULT = FeatureUtils.createKey("flower_default");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOWER_FLOWER_FOREST = FeatureUtils.createKey("flower_flower_forest");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOWER_SWAMP = FeatureUtils.createKey("flower_swamp");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOWER_PLAIN = FeatureUtils.createKey("flower_plain");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOWER_MEADOW = FeatureUtils.createKey("flower_meadow");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOWER_CHERRY = FeatureUtils.createKey("flower_cherry");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOWER_PALE_GARDEN = FeatureUtils.createKey("flower_pale_garden");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILDFLOWERS_BIRCH_FOREST = FeatureUtils.createKey("wildflowers_birch_forest");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILDFLOWERS_MEADOW = FeatureUtils.createKey("wildflowers_meadow");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FOREST_FLOWERS = FeatureUtils.createKey("forest_flowers");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PALE_FOREST_FLOWERS = FeatureUtils.createKey("pale_forest_flowers");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DARK_FOREST_VEGETATION = FeatureUtils.createKey("dark_forest_vegetation");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PALE_GARDEN_VEGETATION = FeatureUtils.createKey("pale_garden_vegetation");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PALE_MOSS_VEGETATION = FeatureUtils.createKey("pale_moss_vegetation");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PALE_MOSS_PATCH = FeatureUtils.createKey("pale_moss_patch");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PALE_MOSS_PATCH_BONEMEAL = FeatureUtils.createKey("pale_moss_patch_bonemeal");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_FLOWER_FOREST = FeatureUtils.createKey("trees_flower_forest");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MEADOW_TREES = FeatureUtils.createKey("meadow_trees");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_TAIGA = FeatureUtils.createKey("trees_taiga");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_BADLANDS = FeatureUtils.createKey("trees_badlands");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_GROVE = FeatureUtils.createKey("trees_grove");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_SAVANNA = FeatureUtils.createKey("trees_savanna");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_SNOWY = FeatureUtils.createKey("trees_snowy");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_BIRCH = FeatureUtils.createKey("trees_birch");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BIRCH_TALL = FeatureUtils.createKey("birch_tall");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_WINDSWEPT_HILLS = FeatureUtils.createKey("trees_windswept_hills");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_WATER = FeatureUtils.createKey("trees_water");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_BIRCH_AND_OAK_LEAF_LITTER = FeatureUtils.createKey("trees_birch_and_oak_leaf_litter");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_PLAINS = FeatureUtils.createKey("trees_plains");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_SPARSE_JUNGLE = FeatureUtils.createKey("trees_sparse_jungle");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_OLD_GROWTH_SPRUCE_TAIGA = FeatureUtils.createKey("trees_old_growth_spruce_taiga");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_OLD_GROWTH_PINE_TAIGA = FeatureUtils.createKey("trees_old_growth_pine_taiga");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_JUNGLE = FeatureUtils.createKey("trees_jungle");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BAMBOO_VEGETATION = FeatureUtils.createKey("bamboo_vegetation");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MUSHROOM_ISLAND_VEGETATION = FeatureUtils.createKey("mushroom_island_vegetation");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MANGROVE_VEGETATION = FeatureUtils.createKey("mangrove_vegetation");
    private static final float FALLEN_TREE_ONE_IN_CHANCE = 80.0f;

    private static RandomPatchConfiguration grassPatch(BlockStateProvider blockStateProvider, int n) {
        return FeatureUtils.simpleRandomPatchConfiguration(n, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(blockStateProvider)));
    }

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> bootstrapContext) {
        HolderGetter<ConfiguredFeature<?, ?>> holderGetter = bootstrapContext.lookup(Registries.CONFIGURED_FEATURE);
        Holder.Reference<ConfiguredFeature<?, ?>> reference = holderGetter.getOrThrow(TreeFeatures.HUGE_BROWN_MUSHROOM);
        Holder.Reference<ConfiguredFeature<?, ?>> reference2 = holderGetter.getOrThrow(TreeFeatures.HUGE_RED_MUSHROOM);
        Holder.Reference<ConfiguredFeature<?, ?>> reference3 = holderGetter.getOrThrow(TreeFeatures.FANCY_OAK_BEES_005);
        Holder.Reference<ConfiguredFeature<?, ?>> reference4 = holderGetter.getOrThrow(TreeFeatures.OAK_BEES_005);
        Holder.Reference<ConfiguredFeature<?, ?>> reference5 = holderGetter.getOrThrow(PATCH_GRASS_JUNGLE);
        HolderGetter<PlacedFeature> holderGetter2 = bootstrapContext.lookup(Registries.PLACED_FEATURE);
        Holder.Reference<PlacedFeature> reference6 = holderGetter2.getOrThrow(TreePlacements.PALE_OAK_CHECKED);
        Holder.Reference<PlacedFeature> reference7 = holderGetter2.getOrThrow(TreePlacements.PALE_OAK_CREAKING_CHECKED);
        Holder.Reference<PlacedFeature> reference8 = holderGetter2.getOrThrow(TreePlacements.FANCY_OAK_CHECKED);
        Holder.Reference<PlacedFeature> reference9 = holderGetter2.getOrThrow(TreePlacements.BIRCH_BEES_002);
        Holder.Reference<PlacedFeature> reference10 = holderGetter2.getOrThrow(TreePlacements.FANCY_OAK_BEES_002);
        Holder.Reference<PlacedFeature> reference11 = holderGetter2.getOrThrow(TreePlacements.FANCY_OAK_BEES);
        Holder.Reference<PlacedFeature> reference12 = holderGetter2.getOrThrow(TreePlacements.PINE_CHECKED);
        Holder.Reference<PlacedFeature> reference13 = holderGetter2.getOrThrow(TreePlacements.SPRUCE_CHECKED);
        Holder.Reference<PlacedFeature> reference14 = holderGetter2.getOrThrow(TreePlacements.PINE_ON_SNOW);
        Holder.Reference<PlacedFeature> reference15 = holderGetter2.getOrThrow(TreePlacements.ACACIA_CHECKED);
        Holder.Reference<PlacedFeature> reference16 = holderGetter2.getOrThrow(TreePlacements.SUPER_BIRCH_BEES_0002);
        Holder.Reference<PlacedFeature> reference17 = holderGetter2.getOrThrow(TreePlacements.BIRCH_BEES_0002_PLACED);
        Holder.Reference<PlacedFeature> reference18 = holderGetter2.getOrThrow(TreePlacements.BIRCH_BEES_0002_LEAF_LITTER);
        Holder.Reference<PlacedFeature> reference19 = holderGetter2.getOrThrow(TreePlacements.FANCY_OAK_BEES_0002_LEAF_LITTER);
        Holder.Reference<PlacedFeature> reference20 = holderGetter2.getOrThrow(TreePlacements.JUNGLE_BUSH);
        Holder.Reference<PlacedFeature> reference21 = holderGetter2.getOrThrow(TreePlacements.MEGA_SPRUCE_CHECKED);
        Holder.Reference<PlacedFeature> reference22 = holderGetter2.getOrThrow(TreePlacements.MEGA_PINE_CHECKED);
        Holder.Reference<PlacedFeature> reference23 = holderGetter2.getOrThrow(TreePlacements.MEGA_JUNGLE_TREE_CHECKED);
        Holder.Reference<PlacedFeature> reference24 = holderGetter2.getOrThrow(TreePlacements.TALL_MANGROVE_CHECKED);
        Holder.Reference<PlacedFeature> reference25 = holderGetter2.getOrThrow(TreePlacements.OAK_CHECKED);
        Holder.Reference<PlacedFeature> reference26 = holderGetter2.getOrThrow(TreePlacements.OAK_BEES_002);
        Holder.Reference<PlacedFeature> reference27 = holderGetter2.getOrThrow(TreePlacements.SUPER_BIRCH_BEES);
        Holder.Reference<PlacedFeature> reference28 = holderGetter2.getOrThrow(TreePlacements.SPRUCE_ON_SNOW);
        Holder.Reference<PlacedFeature> reference29 = holderGetter2.getOrThrow(TreePlacements.OAK_BEES_0002_LEAF_LITTER);
        Holder.Reference<PlacedFeature> reference30 = holderGetter2.getOrThrow(TreePlacements.JUNGLE_TREE_CHECKED);
        Holder.Reference<PlacedFeature> reference31 = holderGetter2.getOrThrow(TreePlacements.MANGROVE_CHECKED);
        Holder.Reference<PlacedFeature> reference32 = holderGetter2.getOrThrow(TreePlacements.OAK_LEAF_LITTER);
        Holder.Reference<PlacedFeature> reference33 = holderGetter2.getOrThrow(TreePlacements.DARK_OAK_LEAF_LITTER);
        Holder.Reference<PlacedFeature> reference34 = holderGetter2.getOrThrow(TreePlacements.BIRCH_LEAF_LITTER);
        Holder.Reference<PlacedFeature> reference35 = holderGetter2.getOrThrow(TreePlacements.FANCY_OAK_LEAF_LITTER);
        Holder.Reference<PlacedFeature> reference36 = holderGetter2.getOrThrow(TreePlacements.FALLEN_OAK_TREE);
        Holder.Reference<PlacedFeature> reference37 = holderGetter2.getOrThrow(TreePlacements.FALLEN_BIRCH_TREE);
        Holder.Reference<PlacedFeature> reference38 = holderGetter2.getOrThrow(TreePlacements.FALLEN_SUPER_BIRCH_TREE);
        Holder.Reference<PlacedFeature> reference39 = holderGetter2.getOrThrow(TreePlacements.FALLEN_JUNGLE_TREE);
        Holder.Reference<PlacedFeature> reference40 = holderGetter2.getOrThrow(TreePlacements.FALLEN_SPRUCE_TREE);
        FeatureUtils.register(bootstrapContext, BAMBOO_NO_PODZOL, Feature.BAMBOO, new ProbabilityFeatureConfiguration(0.0f));
        FeatureUtils.register(bootstrapContext, BAMBOO_SOME_PODZOL, Feature.BAMBOO, new ProbabilityFeatureConfiguration(0.2f));
        FeatureUtils.register(bootstrapContext, VINES, Feature.VINES);
        FeatureUtils.register(bootstrapContext, PATCH_BROWN_MUSHROOM, Feature.RANDOM_PATCH, FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.BROWN_MUSHROOM))));
        FeatureUtils.register(bootstrapContext, PATCH_RED_MUSHROOM, Feature.RANDOM_PATCH, FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.RED_MUSHROOM))));
        FeatureUtils.register(bootstrapContext, PATCH_SUNFLOWER, Feature.RANDOM_PATCH, FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.SUNFLOWER))));
        FeatureUtils.register(bootstrapContext, PATCH_PUMPKIN, Feature.RANDOM_PATCH, FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.PUMPKIN)), List.of(Blocks.GRASS_BLOCK)));
        FeatureUtils.register(bootstrapContext, PATCH_BERRY_BUSH, Feature.RANDOM_PATCH, FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple((BlockState)Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 3))), List.of(Blocks.GRASS_BLOCK)));
        FeatureUtils.register(bootstrapContext, PATCH_TAIGA_GRASS, Feature.RANDOM_PATCH, VegetationFeatures.grassPatch(new WeightedStateProvider(WeightedList.builder().add(Blocks.SHORT_GRASS.defaultBlockState(), 1).add(Blocks.FERN.defaultBlockState(), 4)), 32));
        FeatureUtils.register(bootstrapContext, PATCH_GRASS, Feature.RANDOM_PATCH, VegetationFeatures.grassPatch(BlockStateProvider.simple(Blocks.SHORT_GRASS), 32));
        FeatureUtils.register(bootstrapContext, PATCH_GRASS_MEADOW, Feature.RANDOM_PATCH, VegetationFeatures.grassPatch(BlockStateProvider.simple(Blocks.SHORT_GRASS), 16));
        FeatureUtils.register(bootstrapContext, PATCH_LEAF_LITTER, Feature.RANDOM_PATCH, FeatureUtils.simpleRandomPatchConfiguration(32, PlacementUtils.filtered(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new WeightedStateProvider(VegetationFeatures.leafLitterPatchBuilder(1, 3))), BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.matchesBlocks(Direction.DOWN.getUnitVec3i(), Blocks.GRASS_BLOCK)))));
        FeatureUtils.register(bootstrapContext, PATCH_GRASS_JUNGLE, Feature.RANDOM_PATCH, new RandomPatchConfiguration(32, 7, 3, PlacementUtils.filtered(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new WeightedStateProvider(WeightedList.builder().add(Blocks.SHORT_GRASS.defaultBlockState(), 3).add(Blocks.FERN.defaultBlockState(), 1))), BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.not(BlockPredicate.matchesBlocks(Direction.DOWN.getUnitVec3i(), Blocks.PODZOL))))));
        FeatureUtils.register(bootstrapContext, SINGLE_PIECE_OF_GRASS, Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.SHORT_GRASS.defaultBlockState())));
        FeatureUtils.register(bootstrapContext, PATCH_DEAD_BUSH, Feature.RANDOM_PATCH, VegetationFeatures.grassPatch(BlockStateProvider.simple(Blocks.DEAD_BUSH), 4));
        FeatureUtils.register(bootstrapContext, PATCH_DRY_GRASS, Feature.RANDOM_PATCH, VegetationFeatures.grassPatch(new WeightedStateProvider(WeightedList.builder().add(Blocks.SHORT_DRY_GRASS.defaultBlockState(), 1).add(Blocks.TALL_DRY_GRASS.defaultBlockState(), 1)), 64));
        FeatureUtils.register(bootstrapContext, PATCH_MELON, Feature.RANDOM_PATCH, new RandomPatchConfiguration(64, 7, 3, PlacementUtils.filtered(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.MELON)), BlockPredicate.allOf(BlockPredicate.replaceable(), BlockPredicate.noFluid(), BlockPredicate.matchesBlocks(Direction.DOWN.getUnitVec3i(), Blocks.GRASS_BLOCK)))));
        FeatureUtils.register(bootstrapContext, PATCH_WATERLILY, Feature.RANDOM_PATCH, new RandomPatchConfiguration(10, 7, 3, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.LILY_PAD)))));
        FeatureUtils.register(bootstrapContext, PATCH_TALL_GRASS, Feature.RANDOM_PATCH, FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.TALL_GRASS))));
        FeatureUtils.register(bootstrapContext, PATCH_LARGE_FERN, Feature.RANDOM_PATCH, FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.LARGE_FERN))));
        FeatureUtils.register(bootstrapContext, PATCH_BUSH, Feature.RANDOM_PATCH, new RandomPatchConfiguration(24, 5, 3, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.BUSH)))));
        FeatureUtils.register(bootstrapContext, PATCH_CACTUS, Feature.RANDOM_PATCH, FeatureUtils.simpleRandomPatchConfiguration(10, PlacementUtils.inlinePlaced(Feature.BLOCK_COLUMN, new BlockColumnConfiguration(List.of(BlockColumnConfiguration.layer(BiasedToBottomInt.of(1, 3), BlockStateProvider.simple(Blocks.CACTUS)), BlockColumnConfiguration.layer(new WeightedListInt(WeightedList.builder().add(ConstantInt.of(0), 3).add(ConstantInt.of(1), 1).build()), BlockStateProvider.simple(Blocks.CACTUS_FLOWER))), Direction.UP, BlockPredicate.ONLY_IN_AIR_PREDICATE, false), BlockPredicateFilter.forPredicate(BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.wouldSurvive(Blocks.CACTUS.defaultBlockState(), BlockPos.ZERO))))));
        FeatureUtils.register(bootstrapContext, PATCH_SUGAR_CANE, Feature.RANDOM_PATCH, new RandomPatchConfiguration(20, 4, 0, PlacementUtils.inlinePlaced(Feature.BLOCK_COLUMN, BlockColumnConfiguration.simple(BiasedToBottomInt.of(2, 4), BlockStateProvider.simple(Blocks.SUGAR_CANE)), VegetationFeatures.nearWaterPredicate(Blocks.SUGAR_CANE))));
        FeatureUtils.register(bootstrapContext, PATCH_FIREFLY_BUSH, Feature.RANDOM_PATCH, new RandomPatchConfiguration(20, 4, 3, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.FIREFLY_BUSH)))));
        FeatureUtils.register(bootstrapContext, FLOWER_DEFAULT, Feature.FLOWER, VegetationFeatures.grassPatch(new WeightedStateProvider(WeightedList.builder().add(Blocks.POPPY.defaultBlockState(), 2).add(Blocks.DANDELION.defaultBlockState(), 1)), 64));
        FeatureUtils.register(bootstrapContext, FLOWER_FLOWER_FOREST, Feature.FLOWER, new RandomPatchConfiguration(96, 6, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new NoiseProvider(2345L, new NormalNoise.NoiseParameters(0, 1.0, new double[0]), 0.020833334f, List.of(Blocks.DANDELION.defaultBlockState(), Blocks.POPPY.defaultBlockState(), Blocks.ALLIUM.defaultBlockState(), Blocks.AZURE_BLUET.defaultBlockState(), Blocks.RED_TULIP.defaultBlockState(), Blocks.ORANGE_TULIP.defaultBlockState(), Blocks.WHITE_TULIP.defaultBlockState(), Blocks.PINK_TULIP.defaultBlockState(), Blocks.OXEYE_DAISY.defaultBlockState(), Blocks.CORNFLOWER.defaultBlockState(), Blocks.LILY_OF_THE_VALLEY.defaultBlockState()))))));
        FeatureUtils.register(bootstrapContext, FLOWER_SWAMP, Feature.FLOWER, new RandomPatchConfiguration(64, 6, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.BLUE_ORCHID)))));
        FeatureUtils.register(bootstrapContext, FLOWER_PLAIN, Feature.FLOWER, new RandomPatchConfiguration(64, 6, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new NoiseThresholdProvider(2345L, new NormalNoise.NoiseParameters(0, 1.0, new double[0]), 0.005f, -0.8f, 0.33333334f, Blocks.DANDELION.defaultBlockState(), List.of(Blocks.ORANGE_TULIP.defaultBlockState(), Blocks.RED_TULIP.defaultBlockState(), Blocks.PINK_TULIP.defaultBlockState(), Blocks.WHITE_TULIP.defaultBlockState()), List.of(Blocks.POPPY.defaultBlockState(), Blocks.AZURE_BLUET.defaultBlockState(), Blocks.OXEYE_DAISY.defaultBlockState(), Blocks.CORNFLOWER.defaultBlockState()))))));
        FeatureUtils.register(bootstrapContext, FLOWER_MEADOW, Feature.FLOWER, new RandomPatchConfiguration(96, 6, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new DualNoiseProvider(new InclusiveRange<Integer>(1, 3), new NormalNoise.NoiseParameters(-10, 1.0, new double[0]), 1.0f, 2345L, new NormalNoise.NoiseParameters(-3, 1.0, new double[0]), 1.0f, List.of(Blocks.TALL_GRASS.defaultBlockState(), Blocks.ALLIUM.defaultBlockState(), Blocks.POPPY.defaultBlockState(), Blocks.AZURE_BLUET.defaultBlockState(), Blocks.DANDELION.defaultBlockState(), Blocks.CORNFLOWER.defaultBlockState(), Blocks.OXEYE_DAISY.defaultBlockState(), Blocks.SHORT_GRASS.defaultBlockState()))))));
        FeatureUtils.register(bootstrapContext, FLOWER_CHERRY, Feature.FLOWER, new RandomPatchConfiguration(96, 6, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new WeightedStateProvider(VegetationFeatures.flowerBedPatchBuilder(Blocks.PINK_PETALS))))));
        FeatureUtils.register(bootstrapContext, WILDFLOWERS_BIRCH_FOREST, Feature.FLOWER, new RandomPatchConfiguration(64, 6, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new WeightedStateProvider(VegetationFeatures.flowerBedPatchBuilder(Blocks.WILDFLOWERS))))));
        FeatureUtils.register(bootstrapContext, WILDFLOWERS_MEADOW, Feature.FLOWER, new RandomPatchConfiguration(8, 6, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new WeightedStateProvider(VegetationFeatures.flowerBedPatchBuilder(Blocks.WILDFLOWERS))))));
        FeatureUtils.register(bootstrapContext, FLOWER_PALE_GARDEN, Feature.FLOWER, new RandomPatchConfiguration(1, 0, 0, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.CLOSED_EYEBLOSSOM), true))));
        FeatureUtils.register(bootstrapContext, FOREST_FLOWERS, Feature.SIMPLE_RANDOM_SELECTOR, new SimpleRandomFeatureConfiguration(HolderSet.direct(PlacementUtils.inlinePlaced(Feature.RANDOM_PATCH, FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.LILAC))), new PlacementModifier[0]), PlacementUtils.inlinePlaced(Feature.RANDOM_PATCH, FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.ROSE_BUSH))), new PlacementModifier[0]), PlacementUtils.inlinePlaced(Feature.RANDOM_PATCH, FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.PEONY))), new PlacementModifier[0]), PlacementUtils.inlinePlaced(Feature.NO_BONEMEAL_FLOWER, FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.LILY_OF_THE_VALLEY))), new PlacementModifier[0]))));
        FeatureUtils.register(bootstrapContext, PALE_FOREST_FLOWERS, Feature.RANDOM_PATCH, FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.CLOSED_EYEBLOSSOM), true)));
        FeatureUtils.register(bootstrapContext, DARK_FOREST_VEGETATION, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(PlacementUtils.inlinePlaced(reference, new PlacementModifier[0]), 0.025f), new WeightedPlacedFeature(PlacementUtils.inlinePlaced(reference2, new PlacementModifier[0]), 0.05f), new WeightedPlacedFeature(reference33, 0.6666667f), new WeightedPlacedFeature(reference37, 0.0025f), new WeightedPlacedFeature(reference34, 0.2f), new WeightedPlacedFeature(reference36, 0.0125f), new WeightedPlacedFeature(reference35, 0.1f)), reference32));
        FeatureUtils.register(bootstrapContext, PALE_GARDEN_VEGETATION, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(reference7, 0.1f), new WeightedPlacedFeature(reference6, 0.9f)), reference6));
        FeatureUtils.register(bootstrapContext, PALE_MOSS_VEGETATION, Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new WeightedStateProvider(WeightedList.builder().add(Blocks.PALE_MOSS_CARPET.defaultBlockState(), 25).add(Blocks.SHORT_GRASS.defaultBlockState(), 25).add(Blocks.TALL_GRASS.defaultBlockState(), 10))));
        FeatureUtils.register(bootstrapContext, PALE_MOSS_PATCH, Feature.VEGETATION_PATCH, new VegetationPatchConfiguration(BlockTags.MOSS_REPLACEABLE, BlockStateProvider.simple(Blocks.PALE_MOSS_BLOCK), PlacementUtils.inlinePlaced(holderGetter.getOrThrow(PALE_MOSS_VEGETATION), new PlacementModifier[0]), CaveSurface.FLOOR, ConstantInt.of(1), 0.0f, 5, 0.3f, UniformInt.of(2, 4), 0.75f));
        FeatureUtils.register(bootstrapContext, PALE_MOSS_PATCH_BONEMEAL, Feature.VEGETATION_PATCH, new VegetationPatchConfiguration(BlockTags.MOSS_REPLACEABLE, BlockStateProvider.simple(Blocks.PALE_MOSS_BLOCK), PlacementUtils.inlinePlaced(holderGetter.getOrThrow(PALE_MOSS_VEGETATION), new PlacementModifier[0]), CaveSurface.FLOOR, ConstantInt.of(1), 0.0f, 5, 0.6f, UniformInt.of(1, 2), 0.75f));
        FeatureUtils.register(bootstrapContext, TREES_FLOWER_FOREST, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(reference37, 0.0025f), new WeightedPlacedFeature(reference9, 0.2f), new WeightedPlacedFeature(reference10, 0.1f)), reference26));
        FeatureUtils.register(bootstrapContext, MEADOW_TREES, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(reference11, 0.5f)), reference27));
        FeatureUtils.register(bootstrapContext, TREES_TAIGA, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(reference12, 0.33333334f), new WeightedPlacedFeature(reference40, 0.0125f)), reference13));
        FeatureUtils.register(bootstrapContext, TREES_BADLANDS, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(reference36, 0.0125f)), reference32));
        FeatureUtils.register(bootstrapContext, TREES_GROVE, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(reference14, 0.33333334f)), reference28));
        FeatureUtils.register(bootstrapContext, TREES_SAVANNA, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(reference15, 0.8f), new WeightedPlacedFeature(reference36, 0.0125f)), reference25));
        FeatureUtils.register(bootstrapContext, TREES_SNOWY, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(reference40, 0.0125f)), reference13));
        FeatureUtils.register(bootstrapContext, TREES_BIRCH, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(reference37, 0.0125f)), reference17));
        FeatureUtils.register(bootstrapContext, BIRCH_TALL, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(reference38, 0.00625f), new WeightedPlacedFeature(reference16, 0.5f), new WeightedPlacedFeature(reference37, 0.0125f)), reference17));
        FeatureUtils.register(bootstrapContext, TREES_WINDSWEPT_HILLS, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(reference40, 0.008325f), new WeightedPlacedFeature(reference13, 0.666f), new WeightedPlacedFeature(reference8, 0.1f), new WeightedPlacedFeature(reference36, 0.0125f)), reference25));
        FeatureUtils.register(bootstrapContext, TREES_WATER, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(reference8, 0.1f)), reference25));
        FeatureUtils.register(bootstrapContext, TREES_BIRCH_AND_OAK_LEAF_LITTER, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(reference37, 0.0025f), new WeightedPlacedFeature(reference18, 0.2f), new WeightedPlacedFeature(reference19, 0.1f), new WeightedPlacedFeature(reference36, 0.0125f)), reference29));
        FeatureUtils.register(bootstrapContext, TREES_PLAINS, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(PlacementUtils.inlinePlaced(reference3, new PlacementModifier[0]), 0.33333334f), new WeightedPlacedFeature(reference36, 0.0125f)), PlacementUtils.inlinePlaced(reference4, new PlacementModifier[0])));
        FeatureUtils.register(bootstrapContext, TREES_SPARSE_JUNGLE, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(reference8, 0.1f), new WeightedPlacedFeature(reference20, 0.5f), new WeightedPlacedFeature(reference39, 0.0125f)), reference30));
        FeatureUtils.register(bootstrapContext, TREES_OLD_GROWTH_SPRUCE_TAIGA, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(reference21, 0.33333334f), new WeightedPlacedFeature(reference12, 0.33333334f), new WeightedPlacedFeature(reference40, 0.0125f)), reference13));
        FeatureUtils.register(bootstrapContext, TREES_OLD_GROWTH_PINE_TAIGA, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(reference21, 0.025641026f), new WeightedPlacedFeature(reference22, 0.30769232f), new WeightedPlacedFeature(reference12, 0.33333334f), new WeightedPlacedFeature(reference40, 0.0125f)), reference13));
        FeatureUtils.register(bootstrapContext, TREES_JUNGLE, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(reference8, 0.1f), new WeightedPlacedFeature(reference20, 0.5f), new WeightedPlacedFeature(reference23, 0.33333334f), new WeightedPlacedFeature(reference39, 0.0125f)), reference30));
        FeatureUtils.register(bootstrapContext, BAMBOO_VEGETATION, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(reference8, 0.05f), new WeightedPlacedFeature(reference20, 0.15f), new WeightedPlacedFeature(reference23, 0.7f)), PlacementUtils.inlinePlaced(reference5, new PlacementModifier[0])));
        FeatureUtils.register(bootstrapContext, MUSHROOM_ISLAND_VEGETATION, Feature.RANDOM_BOOLEAN_SELECTOR, new RandomBooleanFeatureConfiguration(PlacementUtils.inlinePlaced(reference2, new PlacementModifier[0]), PlacementUtils.inlinePlaced(reference, new PlacementModifier[0])));
        FeatureUtils.register(bootstrapContext, MANGROVE_VEGETATION, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(reference24, 0.85f)), reference31));
    }

    private static WeightedList.Builder<BlockState> flowerBedPatchBuilder(Block block) {
        return VegetationFeatures.segmentedBlockPatchBuilder(block, 1, 4, FlowerBedBlock.AMOUNT, FlowerBedBlock.FACING);
    }

    public static WeightedList.Builder<BlockState> leafLitterPatchBuilder(int n, int n2) {
        return VegetationFeatures.segmentedBlockPatchBuilder(Blocks.LEAF_LITTER, n, n2, LeafLitterBlock.AMOUNT, LeafLitterBlock.FACING);
    }

    private static WeightedList.Builder<BlockState> segmentedBlockPatchBuilder(Block block, int n, int n2, IntegerProperty integerProperty, EnumProperty<Direction> enumProperty) {
        WeightedList.Builder<BlockState> builder = WeightedList.builder();
        for (int i = n; i <= n2; ++i) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                builder.add((BlockState)((BlockState)block.defaultBlockState().setValue(integerProperty, i)).setValue(enumProperty, direction), 1);
            }
        }
        return builder;
    }

    public static BlockPredicateFilter nearWaterPredicate(Block block) {
        return BlockPredicateFilter.forPredicate(BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.wouldSurvive(block.defaultBlockState(), BlockPos.ZERO), BlockPredicate.anyOf(BlockPredicate.matchesFluids((Vec3i)new BlockPos(1, -1, 0), Fluids.WATER, Fluids.FLOWING_WATER), BlockPredicate.matchesFluids((Vec3i)new BlockPos(-1, -1, 0), Fluids.WATER, Fluids.FLOWING_WATER), BlockPredicate.matchesFluids((Vec3i)new BlockPos(0, -1, 1), Fluids.WATER, Fluids.FLOWING_WATER), BlockPredicate.matchesFluids((Vec3i)new BlockPos(0, -1, -1), Fluids.WATER, Fluids.FLOWING_WATER))));
    }
}

