/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.AllOfPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.AnyOfPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.blockpredicates.HasSturdyFacePredicate;
import net.minecraft.world.level.levelgen.blockpredicates.InsideWorldBoundsPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.MatchingBlockTagPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.MatchingBlocksPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.MatchingFluidsPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.NotPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.ReplaceablePredicate;
import net.minecraft.world.level.levelgen.blockpredicates.SolidPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.TrueBlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.UnobstructedPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.WouldSurvivePredicate;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public interface BlockPredicate
extends BiPredicate<WorldGenLevel, BlockPos> {
    public static final Codec<BlockPredicate> CODEC = BuiltInRegistries.BLOCK_PREDICATE_TYPE.byNameCodec().dispatch(BlockPredicate::type, BlockPredicateType::codec);
    public static final BlockPredicate ONLY_IN_AIR_PREDICATE = BlockPredicate.matchesBlocks(Blocks.AIR);
    public static final BlockPredicate ONLY_IN_AIR_OR_WATER_PREDICATE = BlockPredicate.matchesBlocks(Blocks.AIR, Blocks.WATER);

    public BlockPredicateType<?> type();

    public static BlockPredicate allOf(List<BlockPredicate> list) {
        return new AllOfPredicate(list);
    }

    public static BlockPredicate allOf(BlockPredicate ... blockPredicateArray) {
        return BlockPredicate.allOf(List.of(blockPredicateArray));
    }

    public static BlockPredicate allOf(BlockPredicate blockPredicate, BlockPredicate blockPredicate2) {
        return BlockPredicate.allOf(List.of(blockPredicate, blockPredicate2));
    }

    public static BlockPredicate anyOf(List<BlockPredicate> list) {
        return new AnyOfPredicate(list);
    }

    public static BlockPredicate anyOf(BlockPredicate ... blockPredicateArray) {
        return BlockPredicate.anyOf(List.of(blockPredicateArray));
    }

    public static BlockPredicate anyOf(BlockPredicate blockPredicate, BlockPredicate blockPredicate2) {
        return BlockPredicate.anyOf(List.of(blockPredicate, blockPredicate2));
    }

    public static BlockPredicate matchesBlocks(Vec3i vec3i, List<Block> list) {
        return new MatchingBlocksPredicate(vec3i, HolderSet.direct(Block::builtInRegistryHolder, list));
    }

    public static BlockPredicate matchesBlocks(List<Block> list) {
        return BlockPredicate.matchesBlocks(Vec3i.ZERO, list);
    }

    public static BlockPredicate matchesBlocks(Vec3i vec3i, Block ... blockArray) {
        return BlockPredicate.matchesBlocks(vec3i, List.of(blockArray));
    }

    public static BlockPredicate matchesBlocks(Block ... blockArray) {
        return BlockPredicate.matchesBlocks(Vec3i.ZERO, blockArray);
    }

    public static BlockPredicate matchesTag(Vec3i vec3i, TagKey<Block> tagKey) {
        return new MatchingBlockTagPredicate(vec3i, tagKey);
    }

    public static BlockPredicate matchesTag(TagKey<Block> tagKey) {
        return BlockPredicate.matchesTag(Vec3i.ZERO, tagKey);
    }

    public static BlockPredicate matchesFluids(Vec3i vec3i, List<Fluid> list) {
        return new MatchingFluidsPredicate(vec3i, HolderSet.direct(Fluid::builtInRegistryHolder, list));
    }

    public static BlockPredicate matchesFluids(Vec3i vec3i, Fluid ... fluidArray) {
        return BlockPredicate.matchesFluids(vec3i, List.of(fluidArray));
    }

    public static BlockPredicate matchesFluids(Fluid ... fluidArray) {
        return BlockPredicate.matchesFluids(Vec3i.ZERO, fluidArray);
    }

    public static BlockPredicate not(BlockPredicate blockPredicate) {
        return new NotPredicate(blockPredicate);
    }

    public static BlockPredicate replaceable(Vec3i vec3i) {
        return new ReplaceablePredicate(vec3i);
    }

    public static BlockPredicate replaceable() {
        return BlockPredicate.replaceable(Vec3i.ZERO);
    }

    public static BlockPredicate wouldSurvive(BlockState blockState, Vec3i vec3i) {
        return new WouldSurvivePredicate(vec3i, blockState);
    }

    public static BlockPredicate hasSturdyFace(Vec3i vec3i, Direction direction) {
        return new HasSturdyFacePredicate(vec3i, direction);
    }

    public static BlockPredicate hasSturdyFace(Direction direction) {
        return BlockPredicate.hasSturdyFace(Vec3i.ZERO, direction);
    }

    public static BlockPredicate solid(Vec3i vec3i) {
        return new SolidPredicate(vec3i);
    }

    public static BlockPredicate solid() {
        return BlockPredicate.solid(Vec3i.ZERO);
    }

    public static BlockPredicate noFluid() {
        return BlockPredicate.noFluid(Vec3i.ZERO);
    }

    public static BlockPredicate noFluid(Vec3i vec3i) {
        return BlockPredicate.matchesFluids(vec3i, Fluids.EMPTY);
    }

    public static BlockPredicate insideWorld(Vec3i vec3i) {
        return new InsideWorldBoundsPredicate(vec3i);
    }

    public static BlockPredicate alwaysTrue() {
        return TrueBlockPredicate.INSTANCE;
    }

    public static BlockPredicate unobstructed(Vec3i vec3i) {
        return new UnobstructedPredicate(vec3i);
    }

    public static BlockPredicate unobstructed() {
        return BlockPredicate.unobstructed(Vec3i.ZERO);
    }
}

