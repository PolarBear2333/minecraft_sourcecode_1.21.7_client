/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CryingObsidianBlock
extends Block {
    public static final MapCodec<CryingObsidianBlock> CODEC = CryingObsidianBlock.simpleCodec(CryingObsidianBlock::new);

    public MapCodec<CryingObsidianBlock> codec() {
        return CODEC;
    }

    public CryingObsidianBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (randomSource.nextInt(5) != 0) {
            return;
        }
        Direction direction = Direction.getRandom(randomSource);
        if (direction == Direction.UP) {
            return;
        }
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState2 = level.getBlockState(blockPos2);
        if (blockState.canOcclude() && blockState2.isFaceSturdy(level, blockPos2, direction.getOpposite())) {
            return;
        }
        double d = direction.getStepX() == 0 ? randomSource.nextDouble() : 0.5 + (double)direction.getStepX() * 0.6;
        double d2 = direction.getStepY() == 0 ? randomSource.nextDouble() : 0.5 + (double)direction.getStepY() * 0.6;
        double d3 = direction.getStepZ() == 0 ? randomSource.nextDouble() : 0.5 + (double)direction.getStepZ() * 0.6;
        level.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, (double)blockPos.getX() + d, (double)blockPos.getY() + d2, (double)blockPos.getZ() + d3, 0.0, 0.0, 0.0);
    }
}

