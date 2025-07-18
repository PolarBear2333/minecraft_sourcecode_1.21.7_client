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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WetSpongeBlock
extends Block {
    public static final MapCodec<WetSpongeBlock> CODEC = WetSpongeBlock.simpleCodec(WetSpongeBlock::new);

    public MapCodec<WetSpongeBlock> codec() {
        return CODEC;
    }

    protected WetSpongeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (level.dimensionType().ultraWarm()) {
            level.setBlock(blockPos, Blocks.SPONGE.defaultBlockState(), 3);
            level.levelEvent(2009, blockPos, 0);
            level.playSound(null, blockPos, SoundEvents.WET_SPONGE_DRIES, SoundSource.BLOCKS, 1.0f, (1.0f + level.getRandom().nextFloat() * 0.2f) * 0.7f);
        }
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        Direction direction = Direction.getRandom(randomSource);
        if (direction == Direction.UP) {
            return;
        }
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState2 = level.getBlockState(blockPos2);
        if (blockState.canOcclude() && blockState2.isFaceSturdy(level, blockPos2, direction.getOpposite())) {
            return;
        }
        double d = blockPos.getX();
        double d2 = blockPos.getY();
        double d3 = blockPos.getZ();
        if (direction == Direction.DOWN) {
            d2 -= 0.05;
            d += randomSource.nextDouble();
            d3 += randomSource.nextDouble();
        } else {
            d2 += randomSource.nextDouble() * 0.8;
            if (direction.getAxis() == Direction.Axis.X) {
                d3 += randomSource.nextDouble();
                d = direction == Direction.EAST ? (d += 1.1) : (d += 0.05);
            } else {
                d += randomSource.nextDouble();
                d3 = direction == Direction.SOUTH ? (d3 += 1.1) : (d3 += 0.05);
            }
        }
        level.addParticle(ParticleTypes.DRIPPING_WATER, d, d2, d3, 0.0, 0.0, 0.0);
    }
}

