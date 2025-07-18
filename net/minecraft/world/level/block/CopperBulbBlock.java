/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;

public class CopperBulbBlock
extends Block {
    public static final MapCodec<CopperBulbBlock> CODEC = CopperBulbBlock.simpleCodec(CopperBulbBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    protected MapCodec<? extends CopperBulbBlock> codec() {
        return CODEC;
    }

    public CopperBulbBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(LIT, false)).setValue(POWERED, false));
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState2.getBlock() != blockState.getBlock() && level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.checkAndFlip(blockState, serverLevel, blockPos);
        }
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.checkAndFlip(blockState, serverLevel, blockPos);
        }
    }

    public void checkAndFlip(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos) {
        boolean bl = serverLevel.hasNeighborSignal(blockPos);
        if (bl == blockState.getValue(POWERED)) {
            return;
        }
        BlockState blockState2 = blockState;
        if (!blockState.getValue(POWERED).booleanValue()) {
            serverLevel.playSound(null, blockPos, (blockState2 = (BlockState)blockState2.cycle(LIT)).getValue(LIT) != false ? SoundEvents.COPPER_BULB_TURN_ON : SoundEvents.COPPER_BULB_TURN_OFF, SoundSource.BLOCKS);
        }
        serverLevel.setBlock(blockPos, (BlockState)blockState2.setValue(POWERED, bl), 3);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, POWERED);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return level.getBlockState(blockPos).getValue(LIT) != false ? 15 : 0;
    }
}

