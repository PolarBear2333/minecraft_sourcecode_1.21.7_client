/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CaveVines {
    public static final VoxelShape SHAPE = Block.column(14.0, 0.0, 16.0);
    public static final BooleanProperty BERRIES = BlockStateProperties.BERRIES;

    public static InteractionResult use(@Nullable Entity entity, BlockState blockState, Level level, BlockPos blockPos) {
        if (blockState.getValue(BERRIES).booleanValue()) {
            Block.popResource(level, blockPos, new ItemStack(Items.GLOW_BERRIES, 1));
            float f = Mth.randomBetween(level.random, 0.8f, 1.2f);
            level.playSound(null, blockPos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0f, f);
            BlockState blockState2 = (BlockState)blockState.setValue(BERRIES, false);
            level.setBlock(blockPos, blockState2, 2);
            level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(entity, blockState2));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public static boolean hasGlowBerries(BlockState blockState) {
        return blockState.hasProperty(BERRIES) && blockState.getValue(BERRIES) != false;
    }

    public static ToIntFunction<BlockState> emission(int n) {
        return blockState -> blockState.getValue(BlockStateProperties.BERRIES) != false ? n : 0;
    }
}

