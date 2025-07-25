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
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class FurnaceBlock
extends AbstractFurnaceBlock {
    public static final MapCodec<FurnaceBlock> CODEC = FurnaceBlock.simpleCodec(FurnaceBlock::new);

    public MapCodec<FurnaceBlock> codec() {
        return CODEC;
    }

    protected FurnaceBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new FurnaceBlockEntity(blockPos, blockState);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return FurnaceBlock.createFurnaceTicker(level, blockEntityType, BlockEntityType.FURNACE);
    }

    @Override
    protected void openContainer(Level level, BlockPos blockPos, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof FurnaceBlockEntity) {
            player.openMenu((MenuProvider)((Object)blockEntity));
            player.awardStat(Stats.INTERACT_WITH_FURNACE);
        }
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (!blockState.getValue(LIT).booleanValue()) {
            return;
        }
        double d = (double)blockPos.getX() + 0.5;
        double d2 = blockPos.getY();
        double d3 = (double)blockPos.getZ() + 0.5;
        if (randomSource.nextDouble() < 0.1) {
            level.playLocalSound(d, d2, d3, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0f, 1.0f, false);
        }
        Direction direction = (Direction)blockState.getValue(FACING);
        Direction.Axis axis = direction.getAxis();
        double d4 = 0.52;
        double d5 = randomSource.nextDouble() * 0.6 - 0.3;
        double d6 = axis == Direction.Axis.X ? (double)direction.getStepX() * 0.52 : d5;
        double d7 = randomSource.nextDouble() * 6.0 / 16.0;
        double d8 = axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.52 : d5;
        level.addParticle(ParticleTypes.SMOKE, d + d6, d2 + d7, d3 + d8, 0.0, 0.0, 0.0);
        level.addParticle(ParticleTypes.FLAME, d + d6, d2 + d7, d3 + d8, 0.0, 0.0, 0.0);
    }
}

