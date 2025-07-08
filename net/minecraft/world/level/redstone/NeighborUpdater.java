/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.redstone;

import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;

public interface NeighborUpdater {
    public static final Direction[] UPDATE_ORDER = new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};

    public void shapeUpdate(Direction var1, BlockState var2, BlockPos var3, BlockPos var4, int var5, int var6);

    public void neighborChanged(BlockPos var1, Block var2, @Nullable Orientation var3);

    public void neighborChanged(BlockState var1, BlockPos var2, Block var3, @Nullable Orientation var4, boolean var5);

    default public void updateNeighborsAtExceptFromFacing(BlockPos blockPos, Block block, @Nullable Direction direction, @Nullable Orientation orientation) {
        for (Direction direction2 : UPDATE_ORDER) {
            if (direction2 == direction) continue;
            this.neighborChanged(blockPos.relative(direction2), block, null);
        }
    }

    public static void executeShapeUpdate(LevelAccessor levelAccessor, Direction direction, BlockPos blockPos, BlockPos blockPos2, BlockState blockState, int n, int n2) {
        BlockState blockState2 = levelAccessor.getBlockState(blockPos);
        if ((n & 0x80) != 0 && blockState2.is(Blocks.REDSTONE_WIRE)) {
            return;
        }
        BlockState blockState3 = blockState2.updateShape(levelAccessor, levelAccessor, blockPos, direction, blockPos2, blockState, levelAccessor.getRandom());
        Block.updateOrDestroy(blockState2, blockState3, levelAccessor, blockPos, n, n2);
    }

    public static void executeUpdate(Level level, BlockState blockState, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        try {
            blockState.handleNeighborChanged(level, blockPos, block, orientation, bl);
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Exception while updating neighbours");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Block being updated");
            crashReportCategory.setDetail("Source block type", () -> {
                try {
                    return String.format(Locale.ROOT, "ID #%s (%s // %s)", BuiltInRegistries.BLOCK.getKey(block), block.getDescriptionId(), block.getClass().getCanonicalName());
                }
                catch (Throwable throwable) {
                    return "ID #" + String.valueOf(BuiltInRegistries.BLOCK.getKey(block));
                }
            });
            CrashReportCategory.populateBlockDetails(crashReportCategory, level, blockPos, blockState);
            throw new ReportedException(crashReport);
        }
    }
}

