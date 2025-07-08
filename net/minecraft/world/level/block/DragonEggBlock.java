/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DragonEggBlock
extends FallingBlock {
    public static final MapCodec<DragonEggBlock> CODEC = DragonEggBlock.simpleCodec(DragonEggBlock::new);
    private static final VoxelShape SHAPE = Block.column(14.0, 0.0, 16.0);

    public MapCodec<DragonEggBlock> codec() {
        return CODEC;
    }

    public DragonEggBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        this.teleport(blockState, level, blockPos);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        this.teleport(blockState, level, blockPos);
    }

    private void teleport(BlockState blockState, Level level, BlockPos blockPos) {
        WorldBorder worldBorder = level.getWorldBorder();
        for (int i = 0; i < 1000; ++i) {
            BlockPos blockPos2 = blockPos.offset(level.random.nextInt(16) - level.random.nextInt(16), level.random.nextInt(8) - level.random.nextInt(8), level.random.nextInt(16) - level.random.nextInt(16));
            if (!level.getBlockState(blockPos2).isAir() || !worldBorder.isWithinBounds(blockPos2)) continue;
            if (level.isClientSide) {
                for (int j = 0; j < 128; ++j) {
                    double d = level.random.nextDouble();
                    float f = (level.random.nextFloat() - 0.5f) * 0.2f;
                    float f2 = (level.random.nextFloat() - 0.5f) * 0.2f;
                    float f3 = (level.random.nextFloat() - 0.5f) * 0.2f;
                    double d2 = Mth.lerp(d, (double)blockPos2.getX(), (double)blockPos.getX()) + (level.random.nextDouble() - 0.5) + 0.5;
                    double d3 = Mth.lerp(d, (double)blockPos2.getY(), (double)blockPos.getY()) + level.random.nextDouble() - 0.5;
                    double d4 = Mth.lerp(d, (double)blockPos2.getZ(), (double)blockPos.getZ()) + (level.random.nextDouble() - 0.5) + 0.5;
                    level.addParticle(ParticleTypes.PORTAL, d2, d3, d4, f, f2, f3);
                }
            } else {
                level.setBlock(blockPos2, blockState, 2);
                level.removeBlock(blockPos, false);
            }
            return;
        }
    }

    @Override
    protected int getDelayAfterPlace() {
        return 5;
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public int getDustColor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return -16777216;
    }
}

