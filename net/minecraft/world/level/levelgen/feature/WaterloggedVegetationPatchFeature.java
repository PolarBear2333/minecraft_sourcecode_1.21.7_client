/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.VegetationPatchFeature;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;

public class WaterloggedVegetationPatchFeature
extends VegetationPatchFeature {
    public WaterloggedVegetationPatchFeature(Codec<VegetationPatchConfiguration> codec) {
        super(codec);
    }

    @Override
    protected Set<BlockPos> placeGroundPatch(WorldGenLevel worldGenLevel, VegetationPatchConfiguration vegetationPatchConfiguration, RandomSource randomSource, BlockPos blockPos, Predicate<BlockState> predicate, int n, int n2) {
        Set<BlockPos> set = super.placeGroundPatch(worldGenLevel, vegetationPatchConfiguration, randomSource, blockPos, predicate, n, n2);
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (BlockPos blockPos2 : set) {
            if (WaterloggedVegetationPatchFeature.isExposed(worldGenLevel, set, blockPos2, mutableBlockPos)) continue;
            hashSet.add(blockPos2);
        }
        for (BlockPos blockPos2 : hashSet) {
            worldGenLevel.setBlock(blockPos2, Blocks.WATER.defaultBlockState(), 2);
        }
        return hashSet;
    }

    private static boolean isExposed(WorldGenLevel worldGenLevel, Set<BlockPos> set, BlockPos blockPos, BlockPos.MutableBlockPos mutableBlockPos) {
        return WaterloggedVegetationPatchFeature.isExposedDirection(worldGenLevel, blockPos, mutableBlockPos, Direction.NORTH) || WaterloggedVegetationPatchFeature.isExposedDirection(worldGenLevel, blockPos, mutableBlockPos, Direction.EAST) || WaterloggedVegetationPatchFeature.isExposedDirection(worldGenLevel, blockPos, mutableBlockPos, Direction.SOUTH) || WaterloggedVegetationPatchFeature.isExposedDirection(worldGenLevel, blockPos, mutableBlockPos, Direction.WEST) || WaterloggedVegetationPatchFeature.isExposedDirection(worldGenLevel, blockPos, mutableBlockPos, Direction.DOWN);
    }

    private static boolean isExposedDirection(WorldGenLevel worldGenLevel, BlockPos blockPos, BlockPos.MutableBlockPos mutableBlockPos, Direction direction) {
        mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
        return !worldGenLevel.getBlockState(mutableBlockPos).isFaceSturdy(worldGenLevel, mutableBlockPos, direction.getOpposite());
    }

    @Override
    protected boolean placeVegetation(WorldGenLevel worldGenLevel, VegetationPatchConfiguration vegetationPatchConfiguration, ChunkGenerator chunkGenerator, RandomSource randomSource, BlockPos blockPos) {
        if (super.placeVegetation(worldGenLevel, vegetationPatchConfiguration, chunkGenerator, randomSource, blockPos.below())) {
            BlockState blockState = worldGenLevel.getBlockState(blockPos);
            if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && !blockState.getValue(BlockStateProperties.WATERLOGGED).booleanValue()) {
                worldGenLevel.setBlock(blockPos, (BlockState)blockState.setValue(BlockStateProperties.WATERLOGGED, true), 2);
            }
            return true;
        }
        return false;
    }
}

