/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.grower;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class TreeGrower {
    private static final Map<String, TreeGrower> GROWERS = new Object2ObjectArrayMap();
    public static final Codec<TreeGrower> CODEC = Codec.stringResolver(treeGrower -> treeGrower.name, GROWERS::get);
    public static final TreeGrower OAK = new TreeGrower("oak", 0.1f, Optional.empty(), Optional.empty(), Optional.of(TreeFeatures.OAK), Optional.of(TreeFeatures.FANCY_OAK), Optional.of(TreeFeatures.OAK_BEES_005), Optional.of(TreeFeatures.FANCY_OAK_BEES_005));
    public static final TreeGrower SPRUCE = new TreeGrower("spruce", 0.5f, Optional.of(TreeFeatures.MEGA_SPRUCE), Optional.of(TreeFeatures.MEGA_PINE), Optional.of(TreeFeatures.SPRUCE), Optional.empty(), Optional.empty(), Optional.empty());
    public static final TreeGrower MANGROVE = new TreeGrower("mangrove", 0.85f, Optional.empty(), Optional.empty(), Optional.of(TreeFeatures.MANGROVE), Optional.of(TreeFeatures.TALL_MANGROVE), Optional.empty(), Optional.empty());
    public static final TreeGrower AZALEA = new TreeGrower("azalea", Optional.empty(), Optional.of(TreeFeatures.AZALEA_TREE), Optional.empty());
    public static final TreeGrower BIRCH = new TreeGrower("birch", Optional.empty(), Optional.of(TreeFeatures.BIRCH), Optional.of(TreeFeatures.BIRCH_BEES_005));
    public static final TreeGrower JUNGLE = new TreeGrower("jungle", Optional.of(TreeFeatures.MEGA_JUNGLE_TREE), Optional.of(TreeFeatures.JUNGLE_TREE_NO_VINE), Optional.empty());
    public static final TreeGrower ACACIA = new TreeGrower("acacia", Optional.empty(), Optional.of(TreeFeatures.ACACIA), Optional.empty());
    public static final TreeGrower CHERRY = new TreeGrower("cherry", Optional.empty(), Optional.of(TreeFeatures.CHERRY), Optional.of(TreeFeatures.CHERRY_BEES_005));
    public static final TreeGrower DARK_OAK = new TreeGrower("dark_oak", Optional.of(TreeFeatures.DARK_OAK), Optional.empty(), Optional.empty());
    public static final TreeGrower PALE_OAK = new TreeGrower("pale_oak", Optional.of(TreeFeatures.PALE_OAK_BONEMEAL), Optional.empty(), Optional.empty());
    private final String name;
    private final float secondaryChance;
    private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> megaTree;
    private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> secondaryMegaTree;
    private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> tree;
    private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> secondaryTree;
    private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> flowers;
    private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> secondaryFlowers;

    public TreeGrower(String string, Optional<ResourceKey<ConfiguredFeature<?, ?>>> optional, Optional<ResourceKey<ConfiguredFeature<?, ?>>> optional2, Optional<ResourceKey<ConfiguredFeature<?, ?>>> optional3) {
        this(string, 0.0f, optional, Optional.empty(), optional2, Optional.empty(), optional3, Optional.empty());
    }

    public TreeGrower(String string, float f, Optional<ResourceKey<ConfiguredFeature<?, ?>>> optional, Optional<ResourceKey<ConfiguredFeature<?, ?>>> optional2, Optional<ResourceKey<ConfiguredFeature<?, ?>>> optional3, Optional<ResourceKey<ConfiguredFeature<?, ?>>> optional4, Optional<ResourceKey<ConfiguredFeature<?, ?>>> optional5, Optional<ResourceKey<ConfiguredFeature<?, ?>>> optional6) {
        this.name = string;
        this.secondaryChance = f;
        this.megaTree = optional;
        this.secondaryMegaTree = optional2;
        this.tree = optional3;
        this.secondaryTree = optional4;
        this.flowers = optional5;
        this.secondaryFlowers = optional6;
        GROWERS.put(string, this);
    }

    @Nullable
    private ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource randomSource, boolean bl) {
        if (randomSource.nextFloat() < this.secondaryChance) {
            if (bl && this.secondaryFlowers.isPresent()) {
                return this.secondaryFlowers.get();
            }
            if (this.secondaryTree.isPresent()) {
                return this.secondaryTree.get();
            }
        }
        if (bl && this.flowers.isPresent()) {
            return this.flowers.get();
        }
        return this.tree.orElse(null);
    }

    @Nullable
    private ResourceKey<ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource randomSource) {
        if (this.secondaryMegaTree.isPresent() && randomSource.nextFloat() < this.secondaryChance) {
            return this.secondaryMegaTree.get();
        }
        return this.megaTree.orElse(null);
    }

    public boolean growTree(ServerLevel serverLevel, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockState blockState, RandomSource randomSource) {
        Object object;
        ResourceKey<ConfiguredFeature<?, ?>> resourceKey = this.getConfiguredMegaFeature(randomSource);
        if (resourceKey != null && (object = (Holder)serverLevel.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).get(resourceKey).orElse(null)) != null) {
            for (int i = 0; i >= -1; --i) {
                for (int j = 0; j >= -1; --j) {
                    if (!TreeGrower.isTwoByTwoSapling(blockState, serverLevel, blockPos, i, j)) continue;
                    ConfiguredFeature configuredFeature = (ConfiguredFeature)object.value();
                    BlockState blockState2 = Blocks.AIR.defaultBlockState();
                    serverLevel.setBlock(blockPos.offset(i, 0, j), blockState2, 260);
                    serverLevel.setBlock(blockPos.offset(i + 1, 0, j), blockState2, 260);
                    serverLevel.setBlock(blockPos.offset(i, 0, j + 1), blockState2, 260);
                    serverLevel.setBlock(blockPos.offset(i + 1, 0, j + 1), blockState2, 260);
                    if (configuredFeature.place(serverLevel, chunkGenerator, randomSource, blockPos.offset(i, 0, j))) {
                        return true;
                    }
                    serverLevel.setBlock(blockPos.offset(i, 0, j), blockState, 260);
                    serverLevel.setBlock(blockPos.offset(i + 1, 0, j), blockState, 260);
                    serverLevel.setBlock(blockPos.offset(i, 0, j + 1), blockState, 260);
                    serverLevel.setBlock(blockPos.offset(i + 1, 0, j + 1), blockState, 260);
                    return false;
                }
            }
        }
        if ((object = this.getConfiguredFeature(randomSource, this.hasFlowers(serverLevel, blockPos))) == null) {
            return false;
        }
        Holder holder = serverLevel.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).get((ResourceKey)object).orElse(null);
        if (holder == null) {
            return false;
        }
        ConfiguredFeature configuredFeature = (ConfiguredFeature)holder.value();
        BlockState blockState3 = serverLevel.getFluidState(blockPos).createLegacyBlock();
        serverLevel.setBlock(blockPos, blockState3, 260);
        if (configuredFeature.place(serverLevel, chunkGenerator, randomSource, blockPos)) {
            if (serverLevel.getBlockState(blockPos) == blockState3) {
                serverLevel.sendBlockUpdated(blockPos, blockState, blockState3, 2);
            }
            return true;
        }
        serverLevel.setBlock(blockPos, blockState, 260);
        return false;
    }

    private static boolean isTwoByTwoSapling(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, int n, int n2) {
        Block block = blockState.getBlock();
        return blockGetter.getBlockState(blockPos.offset(n, 0, n2)).is(block) && blockGetter.getBlockState(blockPos.offset(n + 1, 0, n2)).is(block) && blockGetter.getBlockState(blockPos.offset(n, 0, n2 + 1)).is(block) && blockGetter.getBlockState(blockPos.offset(n + 1, 0, n2 + 1)).is(block);
    }

    private boolean hasFlowers(LevelAccessor levelAccessor, BlockPos blockPos) {
        for (BlockPos blockPos2 : BlockPos.MutableBlockPos.betweenClosed(blockPos.below().north(2).west(2), blockPos.above().south(2).east(2))) {
            if (!levelAccessor.getBlockState(blockPos2).is(BlockTags.FLOWERS)) continue;
            return true;
        }
        return false;
    }
}

