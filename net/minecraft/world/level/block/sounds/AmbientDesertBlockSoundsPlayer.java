/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block.sounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class AmbientDesertBlockSoundsPlayer {
    private static final int IDLE_SOUND_CHANCE = 2100;
    private static final int DRY_GRASS_SOUND_CHANCE = 200;
    private static final int DEAD_BUSH_SOUND_CHANCE = 130;
    private static final int DEAD_BUSH_SOUND_BADLANDS_DECREASED_CHANCE = 3;
    private static final int SURROUNDING_BLOCKS_PLAY_SOUND_THRESHOLD = 3;
    private static final int SURROUNDING_BLOCKS_DISTANCE_HORIZONTAL_CHECK = 8;
    private static final int SURROUNDING_BLOCKS_DISTANCE_VERTICAL_CHECK = 5;
    private static final int HORIZONTAL_DIRECTIONS = 4;

    public static void playAmbientSandSounds(Level level, BlockPos blockPos, RandomSource randomSource) {
        if (!level.getBlockState(blockPos.above()).is(Blocks.AIR)) {
            return;
        }
        if (randomSource.nextInt(2100) == 0 && AmbientDesertBlockSoundsPlayer.shouldPlayAmbientSandSound(level, blockPos)) {
            level.playLocalSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.SAND_IDLE, SoundSource.AMBIENT, 1.0f, 1.0f, false);
        }
    }

    public static void playAmbientDryGrassSounds(Level level, BlockPos blockPos, RandomSource randomSource) {
        if (randomSource.nextInt(200) == 0 && AmbientDesertBlockSoundsPlayer.shouldPlayDesertDryVegetationBlockSounds(level, blockPos.below())) {
            level.playPlayerSound(SoundEvents.DRY_GRASS, SoundSource.AMBIENT, 1.0f, 1.0f);
        }
    }

    public static void playAmbientDeadBushSounds(Level level, BlockPos blockPos, RandomSource randomSource) {
        if (randomSource.nextInt(130) == 0) {
            BlockState blockState = level.getBlockState(blockPos.below());
            if ((blockState.is(Blocks.RED_SAND) || blockState.is(BlockTags.TERRACOTTA)) && randomSource.nextInt(3) != 0) {
                return;
            }
            if (AmbientDesertBlockSoundsPlayer.shouldPlayDesertDryVegetationBlockSounds(level, blockPos.below())) {
                level.playLocalSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.DEAD_BUSH_IDLE, SoundSource.AMBIENT, 1.0f, 1.0f, false);
            }
        }
    }

    public static boolean shouldPlayDesertDryVegetationBlockSounds(Level level, BlockPos blockPos) {
        return level.getBlockState(blockPos).is(BlockTags.TRIGGERS_AMBIENT_DESERT_DRY_VEGETATION_BLOCK_SOUNDS) && level.getBlockState(blockPos.below()).is(BlockTags.TRIGGERS_AMBIENT_DESERT_DRY_VEGETATION_BLOCK_SOUNDS);
    }

    private static boolean shouldPlayAmbientSandSound(Level level, BlockPos blockPos) {
        int n = 0;
        int n2 = 0;
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int n3;
            int n4;
            boolean bl;
            mutableBlockPos.set(blockPos).move(direction, 8);
            if (AmbientDesertBlockSoundsPlayer.columnContainsTriggeringBlock(level, mutableBlockPos) && n++ >= 3) {
                return true;
            }
            if (bl = (n4 = (n3 = 4 - ++n2) + n) >= 3) continue;
            return false;
        }
        return false;
    }

    private static boolean columnContainsTriggeringBlock(Level level, BlockPos.MutableBlockPos mutableBlockPos) {
        int n = level.getHeight(Heightmap.Types.WORLD_SURFACE, mutableBlockPos) - 1;
        if (Math.abs(n - mutableBlockPos.getY()) <= 5) {
            boolean bl = level.getBlockState(mutableBlockPos.setY(n + 1)).isAir();
            return bl && AmbientDesertBlockSoundsPlayer.canTriggerAmbientDesertSandSounds(level.getBlockState(mutableBlockPos.setY(n)));
        }
        mutableBlockPos.move(Direction.UP, 6);
        BlockState blockState = level.getBlockState(mutableBlockPos);
        mutableBlockPos.move(Direction.DOWN);
        for (int i = 0; i < 10; ++i) {
            BlockState blockState2 = level.getBlockState(mutableBlockPos);
            if (blockState.isAir() && AmbientDesertBlockSoundsPlayer.canTriggerAmbientDesertSandSounds(blockState2)) {
                return true;
            }
            blockState = blockState2;
            mutableBlockPos.move(Direction.DOWN);
        }
        return false;
    }

    private static boolean canTriggerAmbientDesertSandSounds(BlockState blockState) {
        return blockState.is(BlockTags.TRIGGERS_AMBIENT_DESERT_SAND_BLOCK_SOUNDS);
    }
}

