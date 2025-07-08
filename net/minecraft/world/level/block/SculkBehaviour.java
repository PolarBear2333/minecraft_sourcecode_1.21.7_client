/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceSpreadeableBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.SculkVeinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public interface SculkBehaviour {
    public static final SculkBehaviour DEFAULT = new SculkBehaviour(){

        @Override
        public boolean attemptSpreadVein(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, @Nullable Collection<Direction> collection, boolean bl) {
            if (collection == null) {
                return ((SculkVeinBlock)Blocks.SCULK_VEIN).getSameSpaceSpreader().spreadAll(levelAccessor.getBlockState(blockPos), levelAccessor, blockPos, bl) > 0L;
            }
            if (!collection.isEmpty()) {
                if (blockState.isAir() || blockState.getFluidState().is(Fluids.WATER)) {
                    return SculkVeinBlock.regrow(levelAccessor, blockPos, blockState, collection);
                }
                return false;
            }
            return SculkBehaviour.super.attemptSpreadVein(levelAccessor, blockPos, blockState, collection, bl);
        }

        @Override
        public int attemptUseCharge(SculkSpreader.ChargeCursor chargeCursor, LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource, SculkSpreader sculkSpreader, boolean bl) {
            return chargeCursor.getDecayDelay() > 0 ? chargeCursor.getCharge() : 0;
        }

        @Override
        public int updateDecayDelay(int n) {
            return Math.max(n - 1, 0);
        }
    };

    default public byte getSculkSpreadDelay() {
        return 1;
    }

    default public void onDischarged(LevelAccessor levelAccessor, BlockState blockState, BlockPos blockPos, RandomSource randomSource) {
    }

    default public boolean depositCharge(LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource) {
        return false;
    }

    default public boolean attemptSpreadVein(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, @Nullable Collection<Direction> collection, boolean bl) {
        return ((MultifaceSpreadeableBlock)Blocks.SCULK_VEIN).getSpreader().spreadAll(blockState, levelAccessor, blockPos, bl) > 0L;
    }

    default public boolean canChangeBlockStateOnSpread() {
        return true;
    }

    default public int updateDecayDelay(int n) {
        return 1;
    }

    public int attemptUseCharge(SculkSpreader.ChargeCursor var1, LevelAccessor var2, BlockPos var3, RandomSource var4, SculkSpreader var5, boolean var6);
}

